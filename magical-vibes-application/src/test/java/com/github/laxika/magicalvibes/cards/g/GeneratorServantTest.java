package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneratorServant.class, RuneclawBear.class, Divination.class, ManaReflection.class})
class GeneratorServantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Generator Servant adds {C}{C}")
    void activatingAddsTwoColorless() {
        addCreatureReady(player1, new GeneratorServant());

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(findPermanents(player1, "Generator Servant")).isEmpty();
    }

    @Test
    @DisplayName("A creature spell paid for with Generator Servant's mana gains haste")
    void creatureCastWithServantManaGainsHaste() {
        addCreatureReady(player1, new GeneratorServant());
        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Runeclaw Bear").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature spell paid for with ordinary mana does not gain haste")
    void creatureCastWithOrdinaryManaHasNoHaste() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Runeclaw Bear").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Servant mana spent on a noncreature spell does not carry haste to a later creature spell")
    void manaSpentOnNoncreatureSpellDoesNotGrantHasteLater() {
        addCreatureReady(player1, new GeneratorServant());
        harness.activateAbility(player1, 0, null, null);

        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Runeclaw Bear").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Splitting Servant mana between two creature spells grants haste to both")
    void splitManaGrantsHasteToBothCreatures() {
        addCreatureReady(player1, new GeneratorServant());
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new RuneclawBear(), new RuneclawBear()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Runeclaw Bear"))
                .hasSize(2)
                .allSatisfy(creature -> assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue());
    }

    @Test
    @DisplayName("Haste granted by Servant mana expires at the end of the turn")
    void grantedHasteExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GeneratorServant());
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Runeclaw Bear");
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick Generator Servant cannot activate its tap ability")
    void summoningSickServantCannotActivate() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new GeneratorServant());
        servant.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Generator Servant");
        harness.assertNotInGraveyard(player1, "Generator Servant");
        assertThat(servant.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Tapped Generator Servant cannot activate its tap ability")
    void tappedServantCannotActivate() {
        Permanent servant = addCreatureReady(player1, new GeneratorServant());
        servant.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Generator Servant");
        harness.assertNotInGraveyard(player1, "Generator Servant");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Mana Reflection doubles Generator Servant's mana production")
    void manaReflectionDoublesServantMana() {
        addCreatureReady(player1, new GeneratorServant());
        harness.addToBattlefield(player1, new ManaReflection());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Generator Servant");
        assertThat(gd.stack).isEmpty();
    }
}
