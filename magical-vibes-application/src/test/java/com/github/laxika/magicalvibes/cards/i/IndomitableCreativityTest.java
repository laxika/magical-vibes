package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndomitableCreativityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys multiple targets and puts one matching card onto each controller's battlefield")
    void destroysTargetsAndReplacesThemForEachController() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new FountainOfYouth()));

        castIndomitableCreativity(2, List.of(ownTarget.getId(), opponentTarget.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1)
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Destroyed permanents controlled by one player grant that player one reveal per permanent")
    void revealsOnceForEachDestroyedPermanent() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Forest(), new FountainOfYouth(), new Forest(), new GrizzlyBears()));

        castIndomitableCreativity(2, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstTarget.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Fountain of Youth"))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2)
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("An indestructible target does not count toward the replacement cards")
    void indestructibleTargetDoesNotCount() {
        Card indestructibleCard = new GrizzlyBears();
        indestructibleCard.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent indestructible = harness.addToBattlefieldAndReturn(player2, indestructibleCard);
        Permanent destroyable = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castIndomitableCreativity(2, List.of(indestructible.getId(), destroyable.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructible)
                .noneMatch(permanent -> permanent.getId().equals(destroyable.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && !permanent.getId().equals(indestructible.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castIndomitableCreativity(int xValue, List<UUID> targetIds) {
        prepareCast(xValue);
        harness.castSorcery(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast(int xValue) {
        harness.setHand(player1, List.of(new IndomitableCreativity()));
        harness.addMana(player1, ManaColor.RED, xValue + 3);
    }
}
