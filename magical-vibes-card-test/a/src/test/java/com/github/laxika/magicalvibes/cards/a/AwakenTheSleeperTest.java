package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AwakenTheSleeper.class, GrizzlyBears.class, LeoninScimitar.class,
        LoxodonWarhammer.class, Pacifism.class})
class AwakenTheSleeperTest extends BaseCardTest {

    @Test
    @DisplayName("Awaken the Sleeper gains control, untaps, and grants haste")
    void gainsControlUntapsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castAwaken(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Awaken the Sleeper may destroy all Equipment attached to the target")
    void mayDestroyAllAttachedEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent scimitar = addAttachedEquipment(player2, new LeoninScimitar(), target);
        Permanent warhammer = addAttachedEquipment(player2, new LoxodonWarhammer(), target);

        castAwaken(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Loxodon Warhammer");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(scimitar.getId(), warhammer.getId());
    }

    @Test
    @DisplayName("Declining Awaken the Sleeper leaves attached Equipment on the battlefield")
    void decliningEquipmentDestructionLeavesEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAttachedEquipment(player2, new LeoninScimitar(), target);

        castAwaken(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Awaken the Sleeper does not offer Equipment destruction when the target is not equipped")
    void doesNotOfferEquipmentDestructionWhenTargetIsNotEquipped() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAwaken(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Awaken the Sleeper cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        enchantment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new AwakenTheSleeper()));
        addManaForAwaken();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castAwaken(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Awaken the Sleeper can untap and grant haste to your own creature")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        castAwaken(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipment destruction preserves Auras and Equipment attached elsewhere")
    void destroysOnlyEquipmentAttachedToTargetRegardlessOfController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        addAttachedEquipment(player1, new LeoninScimitar(), target);
        addAttachedEquipment(player2, new LoxodonWarhammer(), target);
        Permanent otherEquipment = addAttachedEquipment(player2, new LeoninScimitar(), otherCreature);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(target.getId());

        castAwaken(target);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Loxodon Warhammer");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura, otherEquipment);
        assertThat(aura.getAttachedTo()).isEqualTo(target.getId());
        assertThat(otherEquipment.getAttachedTo()).isEqualTo(otherCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("An Aura alone does not make the target equipped")
    void auraAloneDoesNotOfferDestruction() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(target.getId());

        castAwaken(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    private void castAwaken(Permanent target) {
        harness.setHand(player1, List.of(new AwakenTheSleeper()));
        addManaForAwaken();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addManaForAwaken() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addAttachedEquipment(Player player, Card equipment, Permanent target) {
        Permanent permanent = addCreatureReady(player, equipment);
        permanent.setAttachedTo(target.getId());
        return permanent;
    }
}
