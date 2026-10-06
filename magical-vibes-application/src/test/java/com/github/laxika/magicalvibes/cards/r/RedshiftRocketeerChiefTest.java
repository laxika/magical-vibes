package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KolodinTriumphCaster;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Redshift, Rocketeer Chief")
@CardUsed({RedshiftRocketeerChief.class, Forest.class, GrizzlyBears.class, LightningBolt.class,
        BrightfieldGlider.class, KolodinTriumphCaster.class})
class RedshiftRocketeerChiefTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds ability-only mana equal to current power")
    void tapAddsAbilityOnlyManaEqualToPower() {
        Permanent redshift = addReadyRedshift();
        redshift.setPowerModifier(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability-only mana cannot pay for a spell")
    void abilityOnlyManaCannotPayForSpell() {
        addReadyRedshift();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaust puts any number of permanent cards from hand onto the battlefield")
    void exhaustPutsAnyNumberOfPermanentsFromHandOntoBattlefield() {
        addReadyRedshift();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new LightningBolt();
        harness.setHand(player1, List.of(creature, land, spell));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(creature, land);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReadyRedshift();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Tap mana ability cannot be activated while summoning sick")
    void tapManaAbilityRequiresNoSummoningSickness() {
        harness.addToBattlefield(player1, new RedshiftRocketeerChief());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exhaust works while summoning sick and with an empty hand")
    void exhaustWorksWithEmptyHandAndSummoningSickness() {
        harness.addToBattlefield(player1, new RedshiftRocketeerChief());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability-only mana can pay the exhaust activation cost")
    void abilityOnlyManaPaysForExhaust() {
        addReadyRedshift();
        harness.setHand(player1, List.of(new Forest()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Exhaust can stop after putting just one permanent onto the battlefield")
    void exhaustCanPutOnlySomePermanents() {
        addReadyRedshift();
        Card chosen = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of(chosen, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).contains(chosen).doesNotContain(remaining);
    }

    @Test
    @DisplayName("Permanents put onto the battlefield by exhaust see each other's entry")
    void exhaustPermanentsEnterSimultaneously() {
        addReadyRedshift();
        harness.setHand(player1, List.of(new BrightfieldGlider(), new KolodinTriumphCaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Brightfield Glider").isSaddled()).isTrue();
    }

    private Permanent addReadyRedshift() {
        return addCreatureReady(player1, new RedshiftRocketeerChief());
    }
}
