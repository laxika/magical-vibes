package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavagersMace.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class, StoneworkPackbeast.class})
class RavagersMaceTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Ravager's Mace attaches it to target creature you control")
    void enteringAttachesToTargetCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavagersMace()));
        addMaceMana(1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mace = findPermanent(player1, "Ravager's Mace");
        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets party-size power and menace")
    void equippedCreatureGetsPartySizePowerAndMenace() {
        Permanent mace = addCreatureReady(player1, new RavagersMace());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addFullParty();
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Equip {2}{B}{R} attaches Ravager's Mace to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent mace = addCreatureReady(player1, new RavagersMace());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addMaceMana(2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An empty party still grants menace without a power bonus")
    void emptyPartyStillGrantsMenace() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new RavagersMace());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Each multitype creature fills only one party role and the bonus updates")
    void multitypeCreaturesEachFillOnlyOneRole() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new RavagersMace());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        harness.addToBattlefield(player1, new StoneworkPackbeast());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                !permanent.getId().equals(mace.getId()) && !permanent.getId().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The party bonus uses the Equipment controller even on an opposing creature")
    void partyBonusUsesEquipmentController() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new RavagersMace());
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPackbeast());
        harness.addToBattlefield(player2, new StoneworkPackbeast());
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Reequipping moves both the party bonus and menace to the new creature")
    void reequippingMovesBonuses() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new RavagersMace());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        mace.setAttachedTo(first.getId());
        addMaceMana(2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The enters trigger leaves the Equipment unattached if its target leaves")
    void entersTriggerDoesNotAttachWhenTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.setHand(player1, List.of(new RavagersMace()));
        addMaceMana(1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ravager's Mace").getAttachedTo()).isNull();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void addMaceMana(int generic) {
        harness.addMana(player1, ManaColor.COLORLESS, generic);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
