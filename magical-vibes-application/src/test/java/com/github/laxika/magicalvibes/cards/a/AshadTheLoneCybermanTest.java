package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshadTheLoneCyberman.class, GrizzlyBears.class, HowlingMine.class})
class AshadTheLoneCybermanTest extends BaseCardTest {

    @Test
    @DisplayName("The first nonlegendary artifact spell each turn gets casualty 2")
    void firstNonlegendaryArtifactSpellGetsCasualtyTwo() {
        Permanent ashad = harness.addToBattlefieldAndReturn(player1, new AshadTheLoneCyberman());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithCasualty(player1, 0, null, List.of(fodder.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getEffectsToResolve().stream()
                .anyMatch(CopyControllerCastSpellEffect.class::isInstance));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Howling Mine"))
                .hasSize(2);
        assertThat(ashad.getCounters()).containsEntry(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(fodder.getId()));
    }

    @Test
    @DisplayName("Only the first matching artifact spell each turn gets casualty")
    void onlyFirstMatchingArtifactSpellGetsCasualty() {
        harness.addToBattlefield(player1, new AshadTheLoneCyberman());
        Permanent firstFodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondFodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine(), new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithCasualty(player1, 0, null, List.of(firstFodder.getId()));
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, null, List.of(secondFodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
    }

    @Test
    @DisplayName("Nonartifact spells do not use Ashad's casualty allowance")
    void nonartifactSpellDoesNotUseCasualtyAllowance() {
        harness.addToBattlefield(player1, new AshadTheLoneCyberman());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new HowlingMine()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castWithCasualty(player1, 0, null, List.of(fodder.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(fodder.getId()));
    }
}
