package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntwoodsShrieker.class, GrizzlyBears.class, Forest.class})
class HauntwoodsShriekerTest extends BaseCardTest {

    @Test
    void attackingManifestsDread() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shrieker)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void revealsCreatureAndMayTurnItFaceUpForFree() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        int remainingMana = gd.playerManaPools.get(player1.getId()).getTotalAllMana();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(remainingMana);
    }

    @Test
    void mayTurnFaceUpChoiceCanBeDeclined() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isFaceDown()).isTrue();
    }

    @Test
    void nonCreatureFaceDownPermanentCanBeTargetedWithoutMayChoice() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        assertThat(target.isFaceDown()).isTrue();
    }
}
