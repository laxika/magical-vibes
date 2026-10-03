package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConvulsingLicid.class, YouthfulKnight.class, VolrathsStronghold.class})
class ConvulsingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("A summoning-sick Licid cannot pay its tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent licid = harness.addToBattlefieldAndReturn(player1, new ConvulsingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ending the effect requires another red mana payment")
    void cannotEndEffectWithoutMana() {
        Permanent licid = addCreatureReady(player1, new ConvulsingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(bls.canBlock(gd, host)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ending the effect preserves tapping and restores the transformation ability")
    void canTransformAgainAfterEndingEffectAndUntapping() {
        Permanent licid = addCreatureReady(player1, new ConvulsingLicid());
        Permanent firstHost = addCreatureReady(player2, new YouthfulKnight());
        Permanent secondHost = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, firstHost.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        assertThat(licid.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondHost.getId()))
                .isInstanceOf(IllegalStateException.class);

        licid.setTapped(false);
        harness.activateAbility(player1, 0, null, secondHost.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(secondHost.getId());
        assertThat(bls.canBlock(gd, firstHost)).isTrue();
        assertThat(bls.canBlock(gd, secondHost)).isFalse();
    }

    @Test
    @DisplayName("Attached Licid prevents the enchanted creature from blocking")
    void attachedLicidPreventsBlocking() {
        Permanent licid = addCreatureReady(player1, new ConvulsingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        Permanent otherCreature = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(bls.canBlock(gd, host)).isTrue();

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(licid.isTapped()).isTrue();
        assertThat(bls.canBlock(gd, host)).isFalse();
        assertThat(bls.canBlock(gd, otherCreature)).isTrue();
    }

    @Test
    @DisplayName("Attached Licid does not prevent the enchanted creature from attacking")
    void enchantedCreatureCanStillAttack() {
        addCreatureReady(player1, new ConvulsingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, host, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost restores the Licid and allows blocking again")
    void payingEndCostRestoresBlocking() {
        Permanent licid = addCreatureReady(player1, new ConvulsingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).as("ending a Licid effect is a special action").isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(bls.canBlock(gd, host)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new ConvulsingLicid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles and leaves the Licid a creature if its target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addCreatureReady(player1, new ConvulsingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }
}
