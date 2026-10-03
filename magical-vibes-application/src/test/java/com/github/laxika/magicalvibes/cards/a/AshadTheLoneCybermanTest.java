package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AshadTheLoneCyberman.class, GrizzlyBears.class, HowlingMine.class, MarchOfTheMachines.class})
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

        assertThat(findPermanents(player1, "Howling Mine"))
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

    @Test
    @DisplayName("Declining casualty still consumes the first artifact spell allowance")
    void decliningCasualtyConsumesAllowance() {
        Permanent ashad = harness.addToBattlefieldAndReturn(player1, new AshadTheLoneCyberman());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine(), new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(ashad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, null, List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
    }

    @Test
    @DisplayName("An artifact cast before Ashad enters still consumes the allowance")
    void earlierArtifactCastPreventsCasualty() {
        harness.setHand(player1, List.of(new HowlingMine(), new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.addToBattlefield(player1, new AshadTheLoneCyberman());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, null, List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
    }

    @Test
    @DisplayName("Legendary artifact spells do not receive casualty")
    void legendaryArtifactDoesNotReceiveCasualty() {
        harness.addToBattlefield(player1, new AshadTheLoneCyberman());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshadTheLoneCyberman()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, null, List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
    }

    @Test
    @DisplayName("Sacrificing Ashad removes granted casualty before its cast trigger can trigger")
    void sacrificingAshadDoesNotCopySpell() {
        Permanent ashad = harness.addToBattlefieldAndReturn(player1, new AshadTheLoneCyberman());
        harness.setHand(player1, List.of(new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithCasualty(player1, 0, null, List.of(ashad.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ashad, the Lone Cyberman");
        harness.assertNotOnBattlefield(player1, "Ashad, the Lone Cyberman");
        assertThat(findPermanents(player1, "Howling Mine"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing an animated artifact creature puts a counter on Ashad")
    void sacrificingAnimatedArtifactTriggersCounter() {
        Permanent ashad = harness.addToBattlefieldAndReturn(player1, new AshadTheLoneCyberman());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithCasualty(player1, 0, null, List.of(fodder.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Howling Mine");
        assertThat(ashad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A casualty copy of an artifact resolves as a token")
    void artifactCopyBecomesToken() {
        harness.addToBattlefield(player1, new AshadTheLoneCyberman());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithCasualty(player1, 0, null, List.of(fodder.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Howling Mine"))
                .hasSize(2)
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }
}
