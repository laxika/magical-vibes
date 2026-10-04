package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FearOfLostTeeth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PiranhaFly;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskmournsDomination.class, LlanowarElves.class, FountainOfYouth.class,
        PiranhaFly.class, FearOfLostTeeth.class})
class DuskmournsDominationTest extends BaseCardTest {

    @Test
    @DisplayName("Controls the enchanted creature, gives it -3/-0, and removes its abilities")
    void controlsWeakensAndRemovesAbilities() {
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        castDomination(elves);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.hasLostAllAbilities(gd, elves)).isTrue();

        int elvesIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elves);
        assertThatThrownBy(() -> harness.tapPermanent(player1, elvesIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lost its abilities");
    }

    @Test
    @DisplayName("Can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DuskmournsDomination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Removes flying without changing the enchanted creature's toughness")
    void removesFlying() {
        Permanent fly = harness.addToBattlefieldAndReturn(player2, new PiranhaFly());

        castDomination(fly);

        assertThat(gqs.hasKeyword(gd, fly, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, fly)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, fly)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fly);
        assertThat(fly.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Can enchant a creature already controlled by the Aura's controller")
    void canEnchantOwnCreature() {
        Permanent fly = harness.addToBattlefieldAndReturn(player1, new PiranhaFly());

        castDomination(fly);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fly);
        assertThat(gqs.getEffectivePower(gd, fly)).isEqualTo(-1);
        assertThat(gqs.hasKeyword(gd, fly, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Control, power, and abilities revert when the Aura leaves the battlefield")
    void auraLeavingEndsAllItsEffects() {
        Permanent fly = harness.addToBattlefieldAndReturn(player2, new PiranhaFly());
        castDomination(fly);
        Permanent aura = findPermanent(player1, "Duskmourn's Domination");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, aura));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(fly);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fly);
        assertThat(gqs.getEffectivePower(gd, fly)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fly)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, fly, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, fly)).isFalse();
    }

    @Test
    @DisplayName("Does not enter the battlefield when its target leaves before resolution")
    void targetLeavingMakesAuraFailToResolve() {
        Permanent fly = harness.addToBattlefieldAndReturn(player2, new PiranhaFly());
        harness.setHand(player1, List.of(new DuskmournsDomination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, fly.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, fly));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Duskmourn's Domination");
        harness.assertInGraveyard(player1, "Duskmourn's Domination");
    }

    @Test
    @DisplayName("The enchanted creature's removed death ability does not trigger")
    void suppressesDeathAbility() {
        Permanent fear = harness.addToBattlefieldAndReturn(player2, new FearOfLostTeeth());
        castDomination(fear);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fear));
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Fear of Lost Teeth");
        harness.assertInGraveyard(player1, "Duskmourn's Domination");
        harness.assertNotOnBattlefield(player1, "Duskmourn's Domination");
    }

    private void castDomination(Permanent target) {
        harness.setHand(player1, List.of(new DuskmournsDomination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
