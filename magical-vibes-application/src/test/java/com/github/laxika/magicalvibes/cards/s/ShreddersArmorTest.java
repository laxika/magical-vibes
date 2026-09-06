package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddersArmor.class, GrizzlyBears.class, Forest.class})
class ShreddersArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield attaches Shredder's Armor and boosts the target creature")
    void entersAttachedAndBoostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ShreddersArmor armorCard = new ShreddersArmor();
        harness.setHand(player1, List.of(armorCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent armor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == armorCard)
                .findFirst()
                .orElseThrow();
        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability can target only a creature you control")
    void entryAbilityRequiresControlledCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShreddersArmor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip sacrifices a chosen other nonland permanent")
    void equipSacrificesOtherNonlandPermanent() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new ShreddersArmor());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, targetCreature.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(targetCreature.getId(), sacrificed.getId());
        assertThat(choice.validIds()).doesNotContain(armor.getId(), land.getId());

        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(targetCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Equip can be activated only once each turn")
    void equipIsLimitedToOnceEachTurn() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new ShreddersArmor());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondSacrifice);
    }
}
