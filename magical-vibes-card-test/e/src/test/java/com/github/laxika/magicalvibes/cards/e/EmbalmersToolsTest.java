package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.f.FesteringMummy;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.cards.u.UnwaveringInitiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbalmersTools.class, Gravecrawler.class, ReassemblingSkeleton.class,
        FesteringMummy.class, SacredCat.class, UnwaveringInitiate.class, MinimusContainment.class})
class EmbalmersToolsTest extends BaseCardTest {

    // ===== Static: graveyard creature-card ability cost reduction =====

    @Test
    @DisplayName("Creature card's graveyard ability costs {1} less to activate")
    void graveyardCreatureAbilityCostsOneLess() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addToBattlefield(player1, new EmbalmersTools());
        // Reassembling Skeleton normally costs {1}{B}; with Embalmer's Tools it costs just {B}.
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reassembling Skeleton");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without Embalmer's Tools the reduced mana is not enough for the same ability")
    void withoutToolsReducedManaIsInsufficient() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Activated: tap a Zombie, target player mills a card =====

    @Test
    @DisplayName("Tapping a Zombie makes target player mill a card")
    void tapZombieMillsTargetPlayer() {
        harness.addToBattlefield(player1, new EmbalmersTools());
        harness.addToBattlefield(player1, new Gravecrawler());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Gravecrawler").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mill ability without an untapped Zombie")
    void cannotActivateMillWithoutZombie() {
        harness.addToBattlefield(player1, new EmbalmersTools());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleToolsDoNotReduceColoredManaCosts() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addToBattlefield(player1, new EmbalmersTools());
        harness.addToBattlefield(player1, new EmbalmersTools());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Reassembling Skeleton");
        assertThat(findPermanent(player1, "Reassembling Skeleton").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsToolsDoNotReduceYourGraveyardAbilityCost() {
        harness.addToBattlefield(player2, new EmbalmersTools());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void embalmStillRequiresItsColoredMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new EmbalmersTools());
        harness.setGraveyard(player1, List.of(new SacredCat()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Sacred Cat");
        assertThat(findPermanent(player1, "Sacred Cat").getCard().isToken()).isTrue();
    }

    @Test
    void summoningSickZombieCanPayCostAndTargetController() {
        harness.addToBattlefield(player1, new EmbalmersTools());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new FesteringMummy());
        zombie.setSummoningSick(true);
        SacredCat topCard = new SacredCat();
        FesteringMummy nextCard = new FesteringMummy();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(zombie.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void cannotReuseTappedZombieForAnotherActivation() {
        harness.addToBattlefield(player1, new EmbalmersTools());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new FesteringMummy());
        zombie.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapOpponentsZombieToPayCost() {
        harness.addToBattlefield(player1, new EmbalmersTools());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new FesteringMummy());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(zombie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millingEmptyLibraryStillPaysZombieTapCost() {
        harness.addToBattlefield(player1, new EmbalmersTools());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new FesteringMummy());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(zombie.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleToolsStackTheirEmbalmCostReductions() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new EmbalmersTools());
        harness.addToBattlefield(player1, new EmbalmersTools());
        harness.setGraveyard(player1, List.of(new UnwaveringInitiate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Unwavering Initiate");
        assertThat(findPermanent(player1, "Unwavering Initiate").getCard().isToken()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void toolsWithAbilitiesRemovedDoNotReduceEmbalmCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new EmbalmersTools());
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, tools.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new UnwaveringInitiate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInGraveyard(player1, "Unwavering Initiate");
        assertThat(gd.stack).isEmpty();
    }
}
