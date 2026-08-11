package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LullmageMentorTest extends BaseCardTest {

    @Test
    void countersSpellByTappingSevenMerfolkAndMayCreateToken() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new LullmageMentor());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new CoralMerfolk());
        }

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int mentorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);
        harness.activateAbility(player1, mentorIndex, null, shock.getId());

        List<UUID> merfolkIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                .map(Permanent::getId)
                .toList();
        for (UUID merfolkId : merfolkIds.subList(0, 7)) {
            harness.handlePermanentChosen(player1, merfolkId);
        }

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MERFOLK));
    }
}
