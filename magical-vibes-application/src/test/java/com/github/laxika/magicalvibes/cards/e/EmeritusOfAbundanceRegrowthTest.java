package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        Card target = new GrizzlyBears();
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

    private Permanent castEmeritus() {
        harness.setHand(player1, List.of(new EmeritusOfAbundanceRegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EmeritusOfAbundanceRegrowth)
                .findFirst()
                .orElseThrow();
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
