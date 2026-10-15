package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Guile;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnOfferYouCantRefuse.class, Opt.class, GrizzlyBears.class, Guile.class})
class AnOfferYouCantRefuseTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and gives its controller two Treasures")
    void countersNoncreatureSpellAndGivesItsControllerTwoTreasures() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new AnOfferYouCantRefuse()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Rejects a creature spell as a target")
    void rejectsCreatureSpellAsTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AnOfferYouCantRefuse()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    @Test
    @DisplayName("Can counter your own spell and give you two Treasures")
    void countersOwnSpell() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt, new AnOfferYouCantRefuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, opt.getId());

        harness.assertInGraveyard(player1, "Opt");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Creates no additional Treasures when its target has already been countered")
    void createsNoTreasuresWhenTargetIsGone() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new AnOfferYouCantRefuse(), new AnOfferYouCantRefuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.castAndResolveInstant(player2, 0, opt.getId());

        harness.assertInGraveyard(player1, "Opt");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Guile's replacement must offer the free cast before Treasures are created")
    void counterReplacementHappensBeforeTokenCreation() {
        harness.addToBattlefield(player1, new Guile());
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt, new AnOfferYouCantRefuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, opt.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(opt.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }
}
