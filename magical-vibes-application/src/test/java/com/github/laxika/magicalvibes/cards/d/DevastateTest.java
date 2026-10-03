package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.s.SporeFrog;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Devastate.class, SporeFrog.class, DivingGriffin.class, RhysticCave.class, WintermoonMesa.class})
class DevastateTest extends BaseCardTest {

    @Test
    @DisplayName("Devastate destroys the target land and deals 1 damage to each creature and player")
    void destroysLandAndDealsDamageToCreaturesAndPlayers() {
        harness.addToBattlefield(player1, new SporeFrog());
        harness.addToBattlefield(player2, new DivingGriffin());
        harness.addToBattlefield(player2, new RhysticCave());
        harness.addToBattlefield(player1, new WintermoonMesa());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Devastate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Rhystic Cave");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Rhystic Cave");
        harness.assertInGraveyard(player2, "Rhystic Cave");
        harness.assertNotOnBattlefield(player1, "Spore Frog");
        harness.assertOnBattlefield(player2, "Diving Griffin");
        harness.assertOnBattlefield(player1, "Wintermoon Mesa");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Devastate cannot target a nonland permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new DivingGriffin());
        harness.setHand(player1, List.of(new Devastate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Diving Griffin");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Devastate respects a regeneration shield on its target land")
    void respectsRegenerationShieldOnTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Devastate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Wintermoon Mesa");
        harness.assertNotInGraveyard(player1, "Wintermoon Mesa");
    }

    @Test
    @DisplayName("Devastate deals no damage when its only target leaves the battlefield")
    void doesNotResolveWhenTargetLandLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.addToBattlefield(player1, new SporeFrog());
        harness.addToBattlefield(player2, new SporeFrog());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Devastate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spore Frog");
        harness.assertOnBattlefield(player2, "Spore Frog");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Devastate");
    }

    @Test
    @DisplayName("Regenerating the target land does not stop Devastate's damage")
    void dealsDamageEvenWhenTargetLandRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        target.setRegenerationShield(1);
        harness.addToBattlefield(player1, new SporeFrog());
        harness.addToBattlefield(player2, new SporeFrog());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Devastate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Wintermoon Mesa");
        harness.assertInGraveyard(player1, "Spore Frog");
        harness.assertInGraveyard(player2, "Spore Frog");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
