package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Heartseeker.class, CrazedGoblin.class, DarksteelCitadel.class})
class HeartseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {5} attaches Heartseeker to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent heartseeker = addHeartseekerReady(player1);
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(heartseeker.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentCreature() {
        Permanent heartseeker = addHeartseekerReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heartseeker.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipped creature can tap and unattach Heartseeker to destroy a target creature")
    void destroysTargetCreatureAndUnattaches() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(heartseeker.getAttachedTo()).isNull();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Heartseeker's ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(heartseeker.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature loses Heartseeker's ability when Heartseeker is unattached")
    void losesAbilityWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent heartseeker = addHeartseekerReady(player1);
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(creature.isTapped()).isFalse();
        assertThat(heartseeker.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping and unattaching are paid before the target is destroyed")
    void paysCostsBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());
        heartseeker.tap();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(heartseeker.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Crazed Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heartseeker);
    }

    @Test
    @DisplayName("A summoning-sick creature cannot pay the granted tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());
        creature.setSummoningSick(true);
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(heartseeker.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip requires all five mana")
    void insufficientManaPreventsEquip() {
        Permanent heartseeker = addHeartseekerReady(player1);
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(heartseeker.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent heartseeker = addHeartseekerReady(player1);
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(heartseeker.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With two Heartseekers only the one granting the activated ability is unattached")
    void unattachesOnlyTheChosenHeartseeker() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent first = addHeartseekerReady(player1);
        Permanent second = addHeartseekerReady(player1);
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(first.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(second.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The equipped creature can target itself")
    void canDestroyItself() {
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature).contains(heartseeker);
        harness.assertInGraveyard(player1, "Crazed Goblin");
        assertThat(heartseeker.getAttachedTo()).isNull();
    }

    private Permanent addHeartseekerReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Heartseeker());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
