package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DiscoveryDispersal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinaleOfPromise.class, Shock.class, CounselOfTheSoratami.class, DiscoveryDispersal.class})
class FinaleOfPromiseTest extends BaseCardTest {

    @Test
    void graveyardTargetsAreLimitedByXAndByCardType() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(shock, counsel));

        ValidTargetsResponse instantTargets = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), null, 1);
        ValidTargetsResponse sorceryTargetsAtOne = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), List.of(shock.getId()), 1);
        ValidTargetsResponse sorceryTargetsAtThree = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), List.of(shock.getId()), 3);

        assertThat(instantTargets.validGraveyardCardIds()).containsExactly(shock.getId());
        assertThat(sorceryTargetsAtOne.validGraveyardCardIds()).isEmpty();
        assertThat(sorceryTargetsAtThree.validGraveyardCardIds()).containsExactly(counsel.getId());
    }

    @Test
    void splitInstantSorceryMayFillBothTargetSlots() {
        FinaleOfPromise finale = new FinaleOfPromise();
        DiscoveryDispersal splitCard = new DiscoveryDispersal();
        harness.setGraveyard(player1, List.of(splitCard));

        ValidTargetsResponse sorceryTargets = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), List.of(splitCard.getId()), 6);

        assertThat(sorceryTargets.validGraveyardCardIds()).containsExactly(splitCard.getId());
    }

    @Test
    void castsTheChosenInstantForFreeAndExilesIt() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(finale, shock);
    }

    @Test
    void atTenCopiesTheChosenInstantTwice() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castSorcery(player1, 0, 10, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(finale, shock);
    }
}
