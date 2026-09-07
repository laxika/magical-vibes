package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hammerhand;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuckDrubb.class, Boomerang.class, GrizzlyBears.class, Hammerhand.class, LavaAxe.class,
        RavensCrime.class})
class MuckDrubbTest extends BaseCardTest {

    @Test
    @DisplayName("ETB changes a single creature-targeting spell to Muck Drubb")
    void redirectsSingleCreatureTargetingSpellToItself() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);

        MuckDrubb drubb = new MuckDrubb();
        harness.setHand(player1, List.of(drubb));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boomerang.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Muck Drubb");
    }

    @Test
    @DisplayName("A spell targeting a player is not a legal ETB target")
    void doesNotTargetPlayerSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        MuckDrubb drubb = new MuckDrubb();
        harness.setHand(player1, List.of(drubb));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        harness.assertOnBattlefield(player1, "Muck Drubb");
    }

    @Test
    @DisplayName("Repeated target occurrences for one creature are all changed")
    void redirectsRepeatedTargetOccurrences() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Hammerhand hammerhand = new Hammerhand();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(hammerhand));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castEnchantment(player2, 0, List.of(bears.getId(), bears.getId()));
        harness.passPriority(player2);

        MuckDrubb drubb = new MuckDrubb();
        harness.setHand(player1, List.of(drubb));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hammerhand.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent drubbPermanent = findPermanent(player1, "Muck Drubb");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getAttachedTo()).isEqualTo(drubbPermanent.getId()));
    }

    @Test
    @DisplayName("Madness allows Muck Drubb to be cast after it is discarded")
    void castsForMadness() {
        MuckDrubb drubb = discardDrubb();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(drubb.getId()));
    }

    private MuckDrubb discardDrubb() {
        MuckDrubb drubb = new MuckDrubb();
        harness.setHand(player1, List.of(drubb));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return drubb;
    }
}
