package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.r.RhinoBarrelingBrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElectrosBolt.class, Forest.class, LurkingLizards.class, RhinoBarrelingBrute.class})
class ElectrosBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RhinoBarrelingBrute());
        harness.setHand(player1, List.of(new ElectrosBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Electro's Bolt");
    }

    @Test
    @DisplayName("Mayhem casts it from the graveyard for {1}{R} after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        ElectrosBolt bolt = new ElectrosBolt();
        harness.setGraveyard(player1, List.of(bolt));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(bolt.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Lurking Lizards");
        harness.assertInGraveyard(player1, "Electro's Bolt");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ElectrosBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mayhem cannot cast it from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        ElectrosBolt bolt = new ElectrosBolt();
        harness.setGraveyard(player1, List.of(bolt));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mayhem cannot cast the same card again after it resolves without another discard")
    void mayhemCannotRecastAfterResolving() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        ElectrosBolt bolt = new ElectrosBolt();
        harness.setGraveyard(player1, List.of(bolt));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(bolt.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, firstTarget.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstTarget);
        harness.assertInGraveyard(player1, "Electro's Bolt");
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(secondTarget.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Mayhem does not allow casting during an opponent's main phase")
    void mayhemRespectsActivePlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        ElectrosBolt bolt = new ElectrosBolt();
        harness.setGraveyard(player1, List.of(bolt));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(bolt.getId())));
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Electro's Bolt");
    }

    @Test
    @DisplayName("Mayhem does not allow casting while another spell is on the stack")
    void mayhemRequiresEmptyStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RhinoBarrelingBrute());
        ElectrosBolt discardedBolt = new ElectrosBolt();
        harness.setGraveyard(player1, List.of(discardedBolt));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discardedBolt.getId())));
        harness.setHand(player1, List.of(new ElectrosBolt()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Electro's Bolt");
    }

    @Test
    @DisplayName("Discarding another card does not enable this card's mayhem")
    void mayhemRequiresThisSpecificCardToBeDiscarded() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        ElectrosBolt bolt = new ElectrosBolt();
        Forest discardedCard = new Forest();
        harness.setGraveyard(player1, List.of(bolt, discardedCard));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discardedCard.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Electro's Bolt");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
