package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchesEye.class, YokedOx.class})
class WitchesEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Witches' Eye to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent creature = addCreatureReady(player1, new YokedOx());
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new WitchesEye());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(eye.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can pay one and tap to scry 1")
    void equippedCreatureCanScry() {
        Permanent creature = addCreatureReady(player1, new YokedOx());
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new WitchesEye());
        eye.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("An unattached Witches' Eye grants no ability")
    void unattachedEyeGrantsNoAbility() {
        addCreatureReady(player1, new YokedOx());
        harness.addToBattlefield(player1, new WitchesEye());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void canPutTopCardOnBottomAfterEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new YokedOx());
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new WitchesEye());
        eye.setAttachedTo(creature.getId());
        YokedOx top = new YokedOx();
        WitchesEye next = new WitchesEye();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(eye);
        gd.playerGraveyards.get(player1.getId()).add(eye.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YokedOx());
        creature.setSummoningSick(true);
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new WitchesEye());
        eye.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedAbilityRequiresMana() {
        Permanent creature = addCreatureReady(player1, new YokedOx());
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new WitchesEye());
        eye.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedEquipmentStillGrantsAbilityAndEmptyLibraryNeedsNoChoice() {
        Permanent creature = addCreatureReady(player1, new YokedOx());
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new WitchesEye());
        eye.setAttachedTo(creature.getId());
        eye.tap();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
