package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Darkblast;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariGermination.class, ElvesOfDeepShadow.class, Darkblast.class})
class GolgariGerminationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 green Saproling when a nontoken creature you control dies")
    void createsSaprolingWhenOwnNontokenCreatureDies() {
        harness.addToBattlefield(player1, new GolgariGermination());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());

        destroyCreature(creature.getId());

        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(saproling.getCard().getPower()).isEqualTo(1);
        assertThat(saproling.getCard().getToughness()).isEqualTo(1);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Creates one Saproling for each own nontoken creature that dies simultaneously")
    void createsOneSaprolingForEachOwnNontokenCreatureThatDiesSimultaneously() {
        harness.addToBattlefield(player1, new GolgariGermination());
        addCreatureReady(player1, new ElvesOfDeepShadow());
        addCreatureReady(player1, new ElvesOfDeepShadow());
        addCreatureReady(player2, new ElvesOfDeepShadow());
        addCreatureReady(player2, new ElvesOfDeepShadow());

        declareAttackersAndPrepareBlockers(List.of(1, 2));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 2)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for token or opposing creature deaths")
    void doesNotTriggerForTokenOrOpposingCreatureDeaths() {
        harness.addToBattlefield(player1, new GolgariGermination());

        Card tokenCard = new ElvesOfDeepShadow();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ElvesOfDeepShadow());

        destroyCreature(token.getId());
        destroyCreature(opposingCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void destroyCreature(UUID creatureId) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Darkblast()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, creatureId);
        resolveAllTriggers();
    }
}
