package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BloodlustInciter;
import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarshMentor.class, BloodlustInciter.class, EvolvingWilds.class, Censor.class, SacredCat.class})
class HarshMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent activating a creature's non-mana ability takes 2 damage")
    void opponentCreatureNonManaAbilityDeals2Damage() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithNonManaAbility(player2, CardType.CREATURE);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent activating an artifact's non-mana ability takes 2 damage")
    void opponentArtifactNonManaAbilityDeals2Damage() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithNonManaAbility(player2, CardType.ARTIFACT);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A mana ability does not trigger Harsh Mentor")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithAbility(player2, CardType.CREATURE, new AwardManaEffect(ManaColor.GREEN));
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        // Mana ability resolves immediately without using the stack; no trigger, no damage.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An enchantment's non-mana ability does not trigger (not artifact/creature/land)")
    void enchantmentAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        addPermanentWithNonManaAbility(player2, CardType.ENCHANTMENT);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        // The ability is on the stack, but no Harsh Mentor trigger was placed on top of it.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller activating their own creature's ability does not trigger Harsh Mentor")
    void controllerOwnAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        // Harsh Mentor is at index 0, the ability creature at index 1.
        addPermanentWithNonManaAbility(player1, CardType.CREATURE);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A sacrificed land's non-mana ability still triggers before the land ability resolves")
    void sacrificedLandAbilityTriggers() {
        harness.addToBattlefield(player1, new HarshMentor());
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Two Harsh Mentors each deal damage for one opponent activation")
    void multipleMentorsTriggerIndependently() {
        harness.addToBattlefield(player1, new HarshMentor());
        harness.addToBattlefield(player1, new HarshMentor());
        harness.addToBattlefieldAndReturn(player2, new BloodlustInciter()).setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Harsh Mentor"));
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Harsh Mentor trigger still deals damage after its source leaves the battlefield")
    void triggerResolvesAfterMentorLeaves() {
        harness.addToBattlefield(player1, new HarshMentor());
        harness.addToBattlefieldAndReturn(player2, new BloodlustInciter()).setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, harness.getPermanentId(player2, "Bloodlust Inciter"));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cycling from hand does not trigger Harsh Mentor")
    void cyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.activateHandAbility(player2, 0, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Censor");
    }

    @Test
    @DisplayName("Embalm from the graveyard does not trigger Harsh Mentor")
    void embalmDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarshMentor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new SacredCat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.activateGraveyardAbility(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Sacred Cat");
    }

    private void addPermanentWithNonManaAbility(Player player, CardType type) {
        addPermanentWithAbility(player, type, new BoostSelfEffect(1, 0));
    }

    private void addPermanentWithAbility(Player player, CardType type, CardEffect effect) {
        Card card = new Card();
        card.setName("Ability Source");
        card.setType(type);
        card.addActivatedAbility(new ActivatedAbility(true, null, List.of(effect), "{T}: ability."));
        harness.addToBattlefieldAndReturn(player, card).setSummoningSick(false);
    }
}
