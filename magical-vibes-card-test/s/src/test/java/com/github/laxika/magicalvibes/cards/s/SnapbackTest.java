package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.Bewilder;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Snapback.class, Bewilder.class, AshcoatBear.class, PrismaticLens.class})
class SnapbackTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInHand(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Returns a creature to its owner's hand when controlled by another player")
    void returnsControlledCreatureToOwnersHand() {
        AshcoatBear targetCard = new AshcoatBear();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInHand(player1, "Ashcoat Bear");
        harness.assertNotInHand(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Can be cast by exiling a blue card instead of paying mana")
    void castsWithAlternateCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Snapback(), new Bewilder()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInHand(player2, "Ashcoat Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Bewilder");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-blue card")
    void alternateCostRequiresBlueCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Snapback(), new AshcoatBear()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Prismatic Lens")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Alternate cost can exile a blue card preceding Snapback in hand")
    void exilesBlueCardBeforeSpellInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Bewilder payment = new Bewilder();
        AshcoatBear remainingCard = new AshcoatBear();
        harness.setHand(player1, List.of(payment, new Snapback(), remainingCard));

        harness.castInstantWithAlternateExileFromHand(player1, 1, target.getId(), 0);

        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(payment);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingCard);
        harness.assertOnBattlefield(player1, "Ashcoat Bear");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingCard, target.getCard());
        harness.assertInGraveyard(player1, "Snapback");
    }

    @Test
    @DisplayName("Snapback cannot exile itself to pay its alternate cost")
    void cannotExileSpellItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Snapback spell = new Snapback();
        harness.setHand(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Another copy of Snapback can pay the alternate cost")
    void canExileAnotherSnapback() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Snapback spell = new Snapback();
        Snapback payment = new Snapback();
        harness.setHand(player1, List.of(spell, payment));

        harness.castInstantWithAlternateExileFromHand(player1, 0, target.getId(), 1);

        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(payment);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashcoat Bear");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Exiled payment remains exiled when the target leaves before resolution")
    void paymentRemainsExiledWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Bewilder payment = new Bewilder();
        harness.setHand(player1, List.of(new Snapback(), payment));
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithAlternateExileFromHand(player1, 0, target.getId(), 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(payment);
        harness.assertInGraveyard(player1, "Snapback");
        harness.assertInGraveyard(player2, "Snapback");
        assertThat(gd.stack).isEmpty();
    }
}
