package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RaidersKarveTest extends BaseCardTest {

    @Test
    void crewingAnimatesRaidersKarveAndAttackingOffersTopLand() {
        Permanent karve = addRaidersKarveReady();
        Permanent crew = addCreatureReady(player1, new SerraAngel());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(karve.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findPermanent(topLand);
        assertThat(land).isNotNull();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void decliningTopLandLeavesItOnTopOfLibrary() {
        addRaidersKarveReady();
        addCreatureReady(player1, new SerraAngel());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
        assertThat(findPermanent(topLand)).isNull();
    }

    @Test
    void nonlandTopCardStaysOnTopWithoutChoice() {
        addRaidersKarveReady();
        addCreatureReady(player1, new SerraAngel());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    private Permanent addRaidersKarveReady() {
        Permanent karve = new Permanent(new RaidersKarve());
        karve.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(karve);
        return karve;
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
