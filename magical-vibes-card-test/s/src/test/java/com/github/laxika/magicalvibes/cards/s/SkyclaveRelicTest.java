package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyclaveRelic.class, BrokenWings.class, IntoTheRoil.class})
class SkyclaveRelicTest extends BaseCardTest {

    @Test
    void createsNoCopiesWhenNotKicked() {
        harness.setHand(player1, List.of(new SkyclaveRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void createsTwoTappedCopiesWhenKicked() {
        castKickedRelic();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(Permanent::isTapped);
    }

    @Test
    void tokenCopyRetainsManaAbility() {
        castKickedRelic();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        token.untap();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void castKickedRelic() {
        harness.setHand(player1, List.of(new SkyclaveRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void originalProducesChosenColorImmediatelyWithoutUsingStack(ManaColor color) {
        harness.setHand(player1, List.of(new SkyclaveRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void originalAndTokenCopySurviveDestroyEffects() {
        castKickedRelic();
        List<Permanent> relics = gd.playerBattlefields.get(player1.getId());
        Permanent original = relics.stream().filter(p -> !p.getCard().isToken()).findFirst().orElseThrow();
        Permanent token = relics.stream().filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        harness.setHand(player2, List.of(new BrokenWings(), new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player2, 0, original.getId());
        harness.castAndResolveInstant(player2, 0, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original, token).hasSize(3);
    }

    @Test
    void createsCopiesEvenWhenOriginalIsReturnedBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new SkyclaveRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent original = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, original.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(p -> p.getCard().isToken() && p.isTapped());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(original.getCard());
    }
}
