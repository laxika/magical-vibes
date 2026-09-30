package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantCaterpillar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyresisRoach.class, GiantCaterpillar.class, GrizzlyBears.class, Zombify.class})
class PhyresisRoachTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage perpetually grants toxic to Insects in all specified zones")
    void combatDamageGrantsToxicToInsectsAcrossZones() {
        Permanent roach = addCreatureReady(player1, new PhyresisRoach());
        Permanent battlefieldInsect = addCreatureReady(player1, new GiantCaterpillar());
        Permanent nonInsect = addCreatureReady(player1, new GrizzlyBears());
        GiantCaterpillar handInsect = new GiantCaterpillar();
        GiantCaterpillar libraryInsect = new GiantCaterpillar();
        GiantCaterpillar graveyardInsect = new GiantCaterpillar();

        harness.setHand(player1, List.of(handInsect));
        harness.setLibrary(player1, List.of(libraryInsect));
        harness.setGraveyard(player1, List.of(graveyardInsect));
        roach.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, battlefieldInsect, Keyword.TOXIC)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonInsect, Keyword.TOXIC)).isFalse();

        castCreatureAndResolve(handInsect);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        Card drawnInsect = gd.playerHands.get(player1.getId()).getFirst();
        castCreatureAndResolve(drawnInsect);

        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, graveyardInsect.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(handInsect.getId())
                        || permanent.getCard().getId().equals(libraryInsect.getId())
                        || permanent.getCard().getId().equals(graveyardInsect.getId()))
                .allSatisfy(permanent -> assertThat(gqs.hasKeyword(gd, permanent, Keyword.TOXIC)).isTrue());
    }

    private void castCreatureAndResolve(Card card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
