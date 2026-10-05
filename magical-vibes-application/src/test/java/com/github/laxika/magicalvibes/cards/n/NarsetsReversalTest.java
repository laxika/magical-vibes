package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BandTogether;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DovinsVeto;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarsetsReversal.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        DovinsVeto.class, BandTogether.class, ThinkTwice.class})
class NarsetsReversalTest extends BaseCardTest {

    @Test
    void copiesTargetSorceryAndReturnsOriginalToItsOwnersHand() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new NarsetsReversal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry copy = gd.stack.getFirst();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copy.getDescription()).isEqualTo("Copy of Counsel of the Soratami");
        harness.assertInHand(player1, "Counsel of the Soratami");
        harness.assertNotInGraveyard(player1, "Counsel of the Soratami");

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new NarsetsReversal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsUncounterableInstantAndCanKeepOriginalTarget() {
        DovinsVeto veto = new DovinsVeto();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel, new NarsetsReversal()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(veto));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, veto.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player2, "Dovin's Veto");
        harness.assertNotInGraveyard(player2, "Dovin's Veto");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertNotInGraveyard(player1, "Dovin's Veto");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackOriginalIsExiledRatherThanReturnedToHand() {
        ThinkTwice original = new ThinkTwice();
        harness.setGraveyard(player1, List.of(original));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new NarsetsReversal()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, original.getId());

        harness.assertNotInHand(player1, "Think Twice");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(original);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Think Twice");
    }

    @Test
    void canChangeBothDamageTargetAndSourceOfCopiedBandTogether() {
        Permanent oldSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newSource = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent oldTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BandTogether original = new BandTogether();
        harness.setHand(player1, List.of(original));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new NarsetsReversal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(oldTarget.getId(), oldSource.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, original.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newTarget.getId());
        harness.handlePermanentChosen(player2, newSource.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oldSource, oldTarget)
                .doesNotContain(newTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(newSource);
        harness.assertInHand(player1, "Band Together");
    }

    @Test
    void copyCanTargetOriginalSpellBeforeItReturnsToHand() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        DovinsVeto veto = new DovinsVeto();
        harness.setHand(player1, List.of(counsel, new NarsetsReversal()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(veto));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, veto.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, veto.getId());

        harness.assertInHand(player2, "Dovin's Veto");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Counsel of the Soratami");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears);
    }
}
