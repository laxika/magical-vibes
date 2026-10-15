package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.h.HeartOfLight;
import com.github.laxika.magicalvibes.cards.m.MistbladeShinobi;
import com.github.laxika.magicalvibes.cards.v.VeilOfSecrecy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shuriken.class, GnarledMass.class, AngelicCurator.class, MistbladeShinobi.class,
        VeilOfSecrecy.class, HeartOfLight.class})
class ShurikenTest extends BaseCardTest {

    @Test
    @DisplayName("Shuriken deals 2 damage and its target's controller gains it")
    void damagesCreatureAndChangesControl() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shuriken);
    }

    @Test
    @DisplayName("Shuriken stays with its controller when unattached from a Ninja")
    void doesNotChangeControlWhenUnattachedFromNinja() {
        Permanent ninja = addCreatureReady(player1, new GnarledMass());
        TestCards.mutableCard(ninja).setSubtypes(List.of(CardSubtype.NINJA));
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(ninja.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shuriken);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(shuriken);
    }

    @Test
    @DisplayName("Activating Shuriken taps the equipped creature")
    void activationTapsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Shuriken to a creature you control")
    void equipAttachesToCreature() {
        Permanent shuriken = addShurikenReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shuriken.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An unequipped creature does not gain Shuriken's activated ability")
    void unequippedCreatureDoesNotGainAbility() {
        addShurikenReady(player1);
        addCreatureReady(player1, new GnarledMass());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from artifacts prevents Shuriken's damage but not the rest of its ability")
    void protectionFromArtifactsPreventsDamage() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new AngelicCurator());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken);
    }

    @Test
    @DisplayName("Heart of Light on the equipped creature does not prevent Shuriken's damage")
    void creatureDamagePreventionDoesNotApplyToShuriken() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeartOfLight());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken);
    }

    @Test
    @DisplayName("Lethal damage does not prevent the target's controller from gaining Shuriken")
    void transfersControlBeforeLethalDamageIsChecked() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new MistbladeShinobi());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mistblade Shinobi");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken).doesNotContain(target);
    }

    @Test
    @DisplayName("A target gaining shroud stops both damage and control transfer")
    void illegalTargetStopsEntireAbilityButCostsRemainPaid() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player2, List.of(new VeilOfSecrecy()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shuriken);
    }

    @Test
    @DisplayName("Ninja status is remembered from unattachment even if the creature later loses it")
    void remembersNinjaStatusAtActivation() {
        Permanent ninja = addCreatureReady(player1, new MistbladeShinobi());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(ninja.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        ninja.setTransientCreatureTypeOverride(CardSubtype.SPIRIT);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shuriken);
    }

    @Test
    @DisplayName("Unattachment is paid immediately and does not tap Shuriken")
    void paysUnattachmentWhenActivated() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(shuriken.isTapped()).isFalse();
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shuriken);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A summoning-sick creature cannot activate the granted tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        creature.setSummoningSick(true);
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GnarledMass());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shuriken.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpposingCreature() {
        Permanent shuriken = addShurikenReady(player1);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shuriken.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can only be activated at sorcery speed")
    void equipRejectsInstantSpeedActivation() {
        Permanent shuriken = addShurikenReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shuriken.getAttachedTo()).isNull();
    }

    private Permanent addShurikenReady(Player player) {
        return addCreatureReady(player, new Shuriken());
    }
}
