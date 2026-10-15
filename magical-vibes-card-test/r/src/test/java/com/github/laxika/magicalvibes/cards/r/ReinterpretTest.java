package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GaeasRevenge;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Jadzi;
import com.github.laxika.magicalvibes.cards.j.JourneyToTheOracle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reinterpret.class, AirElemental.class, Divination.class, HillGiant.class,
        Fireball.class, GaeasRevenge.class, Jadzi.class, JourneyToTheOracle.class})
class ReinterpretTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndOffersEqualOrLowerManaValueSpell() {
        HillGiant target = new HillGiant();
        HillGiant eligible = new HillGiant();
        AirElemental tooExpensive = new AirElemental();

        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), eligible, tooExpensive));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligible.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(tooExpensive);
    }

    @Test
    void decliningFreeCastLeavesEligibleSpellInHand() {
        HillGiant target = new HillGiant();
        Divination eligible = new Divination();

        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), eligible));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).contains(eligible);
    }

    @Test
    void doesNotOfferSpellAboveTargetManaValue() {
        HillGiant target = new HillGiant();
        AirElemental tooExpensive = new AirElemental();

        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), tooExpensive));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(tooExpensive);
    }

    @Test
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player2, List.of(new Reinterpret()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Hill Giant")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetIsAlreadyCounteredWhenFreeCastIsOffered() {
        HillGiant target = new HillGiant();
        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(target.getId()));
    }

    @Test
    void castsLowerManaValueSorceryDuringOpponentsTurnWithoutMana() {
        HillGiant target = new HillGiant();
        Divination eligible = new Divination();
        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), eligible));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligible.getId());
        harness.assertNotInHand(player2, "Divination");
    }

    @Test
    void acceptingOneEligibleSpellDoesNotOfferAnother() {
        HillGiant target = new HillGiant();
        Divination first = new Divination();
        HillGiant second = new HillGiant();
        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), first, second));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void countersWhenNoCardsRemainInHand() {
        HillGiant target = new HillGiant();
        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Reinterpret");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void targetManaValueIncludesChosenX() {
        Fireball target = new Fireball();
        AirElemental eligible = new AirElemental();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 4, List.of(player2.getId()));
        harness.setHand(player2, List.of(new Reinterpret(), eligible));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Fireball");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligible.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void offersFreeCastEvenWhenTargetCannotBeCountered() {
        GaeasRevenge target = new GaeasRevenge();
        AirElemental eligible = new AirElemental();
        harness.castFromHand(player1, target, "{5}{G}{G}");
        harness.setHand(player2, List.of(new Reinterpret(), eligible));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player1, "Gaea's Revenge");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(target.getId()));
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(eligible.getId());
    }

    @Test
    void offersEligibleBackFaceWhenFrontFaceIsTooExpensive() {
        HillGiant target = new HillGiant();
        Jadzi eligibleBackFace = new Jadzi();
        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), eligibleBackFace));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInGraveyard(player1, "Hill Giant");
    }
}
