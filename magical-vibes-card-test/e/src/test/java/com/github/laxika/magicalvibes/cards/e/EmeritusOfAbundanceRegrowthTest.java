package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeritusOfAbundanceRegrowth.class, Forest.class})
class EmeritusOfAbundanceRegrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Enters prepared with a Regrowth copy")
    void entersPrepared() {
        Permanent emeritus = castEmeritus();

        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(emeritus.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Casting the prepared Regrowth copy returns a graveyard card and unprepares Emeritus")
    void castingPreparedRegrowthReturnsCard() {
        Card target = new EmeritusOfAbundanceRegrowth();
        harness.setGraveyard(player1, List.of(target));
        Permanent emeritus = castEmeritus();
        UUID copyId = emeritus.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Becomes prepared when it attacks with eight lands")
    void becomesPreparedWithEightLands() {
        Permanent emeritus = addCreatureReady(player1, new EmeritusOfAbundanceRegrowth());
        addLands(8);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(emeritus.isPrepared()).isTrue();
    }

    @Test
    @DisplayName("Does not become prepared when it attacks with fewer than eight lands")
    void doesNotBecomePreparedWithFewerThanEightLands() {
        Permanent emeritus = addCreatureReady(player1, new EmeritusOfAbundanceRegrowth());
        addLands(7);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(emeritus.isPrepared()).isFalse();
    }

    @Test
    void entersPreparedWithoutAnEtbTrigger() {
        harness.setHand(player1, List.of(new EmeritusOfAbundanceRegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent emeritus = findPermanent(player1, "Emeritus of Abundance");
        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(emeritus.getPreparedSpellCardId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotBecomePreparedIfLandCountDropsBeforeAttackTriggerResolves() {
        Permanent emeritus = addCreatureReady(player1, new EmeritusOfAbundanceRegrowth());
        addLands(8);
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        Permanent land = gd.playerBattlefields.get(player1.getId()).remove(8);
        gd.playerHands.get(player1.getId()).add(land.getCard());
        resolveAllTriggers();

        assertThat(emeritus.isPrepared()).isFalse();
    }

    @Test
    void preparedRegrowthCanReturnALand() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        Permanent emeritus = castEmeritus();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, emeritus.getPreparedSpellCardId(), target.getId());
        assertThat(emeritus.isPrepared()).isFalse();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    private Permanent castEmeritus() {
        harness.setHand(player1, List.of(new EmeritusOfAbundanceRegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Emeritus of Abundance");
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
