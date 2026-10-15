package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NomadsAssembly.class, GlorySeeker.class, Forest.class})
class NomadsAssemblyTest extends BaseCardTest {

    @Test
    void createsOneSoldierForEachCreatureControlled() {
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GlorySeeker());

        NomadsAssembly card = new NomadsAssembly();
        harness.castFromHand(player1, card, "{4}{W}{W}");
        harness.passBothPriorities();

        List<Permanent> soldiers = soldierTokens(player1);
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.KOR, CardSubtype.SOLDIER);
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        });
        assertThat(soldierTokens(player2)).isEmpty();
    }

    @Test
    void reboundCastsAgainAtNextUpkeepAndReevaluatesCreatureCount() {
        harness.addToBattlefield(player1, new GlorySeeker());

        NomadsAssembly card = new NomadsAssembly();
        harness.castFromHand(player1, card, "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).hasSize(3);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Nomads' Assembly");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void countsCreaturesAtResolutionRatherThanAtCasting() {
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.castFromHand(player1, new NomadsAssembly(), "{4}{W}{W}");
        harness.addToBattlefield(player1, new GlorySeeker());

        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).hasSize(2);
    }

    @Test
    void createsNoTokensWithoutCreaturesButStillRebounds() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GlorySeeker());
        NomadsAssembly card = new NomadsAssembly();
        harness.castFromHand(player1, card, "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.addToBattlefield(player1, new GlorySeeker());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).hasSize(1);
        harness.assertInGraveyard(player1, "Nomads' Assembly");
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOpportunity() {
        NomadsAssembly card = new NomadsAssembly();
        harness.castFromHand(player1, card, "{4}{W}{W}");
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(soldierTokens(player1)).isEmpty();
    }

    private List<Permanent> soldierTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();
    }
}
