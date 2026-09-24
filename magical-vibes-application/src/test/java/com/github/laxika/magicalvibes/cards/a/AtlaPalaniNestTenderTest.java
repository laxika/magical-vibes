package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtlaPalaniNestTender.class, Forest.class, GrizzlyBears.class, Shock.class})
class AtlaPalaniNestTenderTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 0/1 green Egg creature token with defender")
    void createsEggToken() {
        Permanent atla = addReadyAtla();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent egg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(atla.isTapped()).isTrue();
        assertThat(egg.getEffectivePower()).isZero();
        assertThat(egg.getEffectiveToughness()).isEqualTo(1);
        assertThat(egg.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(egg.getCard().getSubtypes()).contains(CardSubtype.EGG);
        assertThat(gqs.hasKeyword(gd, egg, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Puts the first revealed creature onto the battlefield when an Egg dies")
    void putsRevealedCreatureOntoBattlefield() {
        addReadyAtla();
        Permanent egg = createEgg();
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new GrizzlyBears()));

        killWithShock(egg);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Shock");
    }

    @Test
    @DisplayName("Does not put a card onto the battlefield when no creature is found")
    void noCreatureIsFound() {
        addReadyAtla();
        Permanent egg = createEgg();
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        killWithShock(egg);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Shock");
    }

    private Permanent addReadyAtla() {
        return addCreatureReady(player1, new AtlaPalaniNestTender());
    }

    private Permanent createEgg() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
