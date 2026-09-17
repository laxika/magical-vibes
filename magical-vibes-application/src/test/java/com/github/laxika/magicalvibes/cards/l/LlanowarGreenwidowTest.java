package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarGreenwidow.class, Plains.class, Island.class, Swamp.class, Mountain.class,
        Forest.class, Terminate.class})
@DisplayName("Llanowar Greenwidow")
class LlanowarGreenwidowTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard tapped")
    void returnsFromGraveyardTapped() {
        LlanowarGreenwidow widow = new LlanowarGreenwidow();
        harness.setGraveyard(player1, List.of(widow));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Llanowar Greenwidow");
        assertThat(permanent.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Llanowar Greenwidow");
    }

    @Test
    @DisplayName("Domain reduces the graveyard activation cost by each distinct basic land type")
    void domainReducesActivationCost() {
        LlanowarGreenwidow widow = new LlanowarGreenwidow();
        harness.setGraveyard(player1, List.of(widow));
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without enough mana after domain reduction")
    void cannotActivateWithoutEnoughReducedMana() {
        harness.setGraveyard(player1, List.of(new LlanowarGreenwidow()));
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Is exiled instead of going to another zone when it leaves the battlefield")
    void exilesIfItWouldLeaveBattlefield() {
        harness.setGraveyard(player1, List.of(new LlanowarGreenwidow()));
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent widow = findPermanent(player1, "Llanowar Greenwidow");
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, widow.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Greenwidow");
        harness.assertNotInGraveyard(player1, "Llanowar Greenwidow");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Greenwidow"));
    }
}
