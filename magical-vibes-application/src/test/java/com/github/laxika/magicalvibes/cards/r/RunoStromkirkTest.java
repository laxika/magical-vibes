package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrothussLordOfTheDeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunoStromkirk.class, KrothussLordOfTheDeep.class, GrizzlyBears.class,
        ColossalDreadmaw.class})
class RunoStromkirkTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to one target creature card from the graveyard on top of the library")
    void putsTargetCreatureFromGraveyardOnTopOfLibrary() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RunoStromkirk()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
    }

    @Test
    @DisplayName("Transforms when the revealed top card is a creature with mana value at least six")
    void transformsForLargeCreatureOnTop() {
        Permanent runo = addCreatureReady(player1, new RunoStromkirk());
        Card creature = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(runo.isTransformed()).isTrue();
        assertThat(runo.getCard()).isSameAs(runo.getOriginalCard().getBackFaceCard());
    }

    @Test
    @DisplayName("Does not transform for a creature with mana value less than six")
    void doesNotTransformForSmallCreatureOnTop() {
        Permanent runo = addCreatureReady(player1, new RunoStromkirk());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(runo.isTransformed()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
    }

    @Test
    @DisplayName("Creates two tapped and attacking copies when the target is a sea monster")
    void createsTwoCopiesOfSeaMonster() {
        Permanent krothuss = transformedRuno();
        Permanent seaMonster = addCreatureReady(player1, createSeaMonster());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, seaMonster.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> copies = findPermanents(player1, "Sea Serpent");
        List<Permanent> tokenCopies = copies.stream().filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokenCopies).hasSize(2);
        assertThat(tokenCopies).allMatch(Permanent::isTapped).allMatch(Permanent::isAttacking);
        assertThat(krothuss.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Creates one tapped and attacking copy for a non-sea-monster")
    void createsOneCopyOfOtherAttackingCreature() {
        Permanent krothuss = transformedRuno();
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> copies = findPermanents(player1, "Grizzly Bears");
        List<Permanent> tokenCopies = copies.stream().filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokenCopies).hasSize(1);
        assertThat(tokenCopies).allMatch(Permanent::isTapped).allMatch(Permanent::isAttacking);
        assertThat(krothuss.isAttacking()).isTrue();
    }

    private Permanent transformedRuno() {
        Permanent runo = addCreatureReady(player1, new RunoStromkirk());
        runo.setCard(runo.getOriginalCard().getBackFaceCard());
        runo.setTransformed(true);
        return runo;
    }

    private Card createSeaMonster() {
        Card card = new Card();
        card.setName("Sea Serpent");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.SERPENT));
        card.setPower(2);
        card.setToughness(2);
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        return card;
    }
}
