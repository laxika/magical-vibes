package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.CadaverImp;
import com.github.laxika.magicalvibes.cards.g.GelatinousGenesis;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InquisitionOfKozilek.class, Forest.class, GrizzlyBears.class, Peek.class,
        ColossalDreadmaw.class, CadaverImp.class, LagacLizard.class,
        GelatinousGenesis.class, PropheticPrism.class})
class InquisitionOfKozilekTest extends BaseCardTest {

    @Test
    void castingTargetsAPlayer() {
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    void choosesOnlyNonlandCardsWithManaValueAtMostThree() {
        Card eligibleCreature = new GrizzlyBears();
        Card tooExpensive = new ColossalDreadmaw();
        Card land = new Forest();
        Card eligibleSpell = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(eligibleCreature, tooExpensive, land, eligibleSpell)));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(eligibleCreature);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(tooExpensive, land, eligibleSpell);
    }

    @Test
    void doesNothingWhenTargetHasNoEligibleCards() {
        Card tooExpensive = new ColossalDreadmaw();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(tooExpensive, land)));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(tooExpensive, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(tooExpensive, land);
    }

    @Test
    void includesManaValueThreeAndXCardsButExcludesManaValueFourAndLands() {
        Card atLimit = new CadaverImp();
        Card aboveLimit = new LagacLizard();
        Card xSpell = new GelatinousGenesis();
        Card artifact = new PropheticPrism();
        Card land = new Forest();
        harness.setHand(player2, List.of(atLimit, aboveLimit, xSpell, artifact, land));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 2, 3);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(xSpell);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(atLimit, aboveLimit, artifact, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTargetSelfAndDiscardManaValueThreeCard() {
        Card discarded = new CadaverImp();
        Card retained = new Forest();
        harness.setHand(player1, List.of(new InquisitionOfKozilek(), discarded, retained));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesAgainstEmptyHandWithoutRequestingChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Inquisition of Kozilek");
    }

    @Test
    void revealsEntireHandEvenWhenNoCardCanBeChosen() {
        harness.setHand(player2, List.of(new LagacLizard(), new Forest()));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand")
                        && log.contains("Lagac Lizard") && log.contains("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void requiresCasterToChooseAnEligibleCard() {
        Card eligible = new PropheticPrism();
        Card ineligible = new LagacLizard();
        harness.setHand(player2, List.of(eligible, ineligible));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(eligible, ineligible);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(eligible);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(ineligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
