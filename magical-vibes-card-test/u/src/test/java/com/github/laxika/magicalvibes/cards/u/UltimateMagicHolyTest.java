package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimateMagicHoly.class, GrizzlyBears.class, Shock.class, Spellbook.class})
class UltimateMagicHolyTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast grants indestructible to all permanents you control")
    void normalCastGrantsIndestructibleToOwnPermanents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UltimateMagicHoly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(ownArtifact.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Hand cast does not prevent damage to you")
    void handCastDoesNotPreventDamageToController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new UltimateMagicHoly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Foretold cast prevents all damage to you for the turn")
    void foretoldCastPreventsDamageToController() {
        harness.setLife(player1, 20);
        UltimateMagicHoly holy = new UltimateMagicHoly();
        harness.setHand(player1, List.of(holy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        harness.forceActivePlayer(player1);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, holy.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Indestructibility protects existing permanents from lethal damage and expires at cleanup")
    void indestructibilityOnlyAffectsPermanentsPresentAtResolutionAndExpires() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UltimateMagicHoly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(laterCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, protectedCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(protectedCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, protectedCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(protectedCreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Foretold cast grants indestructibility and prevents repeated damage only to its controller")
    void foretoldCastProtectsPermanentsAndOnlyItsController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        UltimateMagicHoly holy = new UltimateMagicHoly();
        harness.setHand(player1, List.of(holy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, holy.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, laterCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(laterCreature);
    }

    @Test
    @DisplayName("Foretold card cannot be cast on the turn it was foretold")
    void cannotCastOnTheTurnItWasForetold() {
        UltimateMagicHoly holy = new UltimateMagicHoly();
        harness.setHand(player1, List.of(holy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, holy.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
