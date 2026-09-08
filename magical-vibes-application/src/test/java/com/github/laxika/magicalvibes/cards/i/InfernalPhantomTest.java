package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalPhantom.class, DazzlingTheaterPropRoom.class, DoomBlade.class, GloriousAnthem.class})
class InfernalPhantomTest extends BaseCardTest {

    @Test
    void getsBoostWhenAnEnchantmentYouControlEnters() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        castSimpleEnchantment(player1);

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    void getsBoostWhenYouFullyUnlockARoom() {
        Permanent room = castRoom();
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathTriggerDealsDamageEqualToItsEffectivePower() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.setLife(player2, 20);
        castSimpleEnchantment(player1);

        destroyWithDoomBlade(phantom);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        castSimpleEnchantment(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    private void castSimpleEnchantment(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(simpleEnchantment()));
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castEnchantment(caster, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void destroyWithDoomBlade(Permanent target) {
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private Card simpleEnchantment() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        return card;
    }
}
