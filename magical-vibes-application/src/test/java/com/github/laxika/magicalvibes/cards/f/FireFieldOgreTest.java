package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.cards.r.RhoxWarMonk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireFieldOgre.class, ResoundingThunder.class, RhoxWarMonk.class})
@DisplayName("Fire-Field Ogre")
class FireFieldOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Fire-Field Ogre to the battlefield with haste")
    void unearthReturnsWithHaste() {
        FireFieldOgre ogre = new FireFieldOgre();
        harness.setGraveyard(player1, List.of(ogre));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Fire-Field Ogre");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Fire-Field Ogre");
    }

    @Test
    @DisplayName("Unearthed Fire-Field Ogre is exiled at the next end step")
    void unearthExiledAtEndStep() {
        FireFieldOgre ogre = new FireFieldOgre();
        harness.setGraveyard(player1, List.of(ogre));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fire-Field Ogre");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fire-Field Ogre"));
    }

    @Test
    void unearthedOgreAttacksImmediatelyAndKillsBlockerBeforeRegularDamage() {
        harness.setGraveyard(player1, List.of(new FireFieldOgre()));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new RhoxWarMonk());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Fire-Field Ogre");
        assertThat(findPermanent(player1, "Fire-Field Ogre").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Rhox War Monk");
        harness.assertLife(player2, 20);
    }

    @Test
    void lethalDamageExilesUnearthedOgre() {
        FireFieldOgre ogre = new FireFieldOgre();
        harness.setGraveyard(player1, List.of(ogre));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Fire-Field Ogre"));

        harness.assertNotOnBattlefield(player1, "Fire-Field Ogre");
        harness.assertNotInGraveyard(player1, "Fire-Field Ogre");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ogre);
    }

    @Test
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new FireFieldOgre()));
        addUnearthMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Fire-Field Ogre");
    }

    @Test
    void unearthRequiresOwnTurn() {
        harness.setGraveyard(player2, List.of(new FireFieldOgre()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Fire-Field Ogre");
    }

    @Test
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new FireFieldOgre()));
        addUnearthMana();
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fire-Field Ogre");
    }

    @Test
    void unearthRequiresEachColoredMana() {
        harness.setGraveyard(player1, List.of(new FireFieldOgre()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Fire-Field Ogre");
        harness.assertNotOnBattlefield(player1, "Fire-Field Ogre");
    }

    private void addUnearthMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
