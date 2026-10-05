package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NihilSpellbomb.class, MoriokReaver.class, GalvanicBlast.class, Shatter.class})
class NihilSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability exiles target player's graveyard")
    void activateAbilityExilesGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver(), new GalvanicBlast()));

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]
        // Per CR 603.3, death triggers from sacrifice resolve first

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline death trigger

        harness.passBothPriorities(); // resolve ExileGraveyard ability

        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Cards should be in exile
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating ability sacrifices the spellbomb")
    void activateAbilitySacrificesSpellbomb() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver()));

        harness.activateAbility(player1, 0, null, player2.getId());

        // Spellbomb should be sacrificed
        harness.assertNotOnBattlefield(player1, "Nihil Spellbomb");
        harness.assertInGraveyard(player1, "Nihil Spellbomb");
    }

    @Test
    @DisplayName("Can target own graveyard")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player1, List.of(new MoriokReaver()));

        harness.activateAbility(player1, 0, null, player1.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        harness.passBothPriorities(); // resolve ExileGraveyard

        // Entire graveyard is exiled (including the spellbomb which was sacrificed as cost)
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Moriok Reaver"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nihil Spellbomb"));
    }

    @Test
    @DisplayName("Works when target player's graveyard is empty")
    void worksOnEmptyGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        harness.passBothPriorities(); // resolve ExileGraveyard - should not error

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting death trigger and paying {B} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Accept death trigger - pay {B}, inner DrawCardEffect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Black mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);

        harness.passBothPriorities(); // resolve ExileGraveyard
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Black mana unspent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        harness.passBothPriorities(); // resolve ExileGraveyard
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver()));
        // No black mana added

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Accept but cannot pay {B} — treated as decline
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        harness.passBothPriorities(); // resolve ExileGraveyard
    }

    @Test
    @DisplayName("Destruction also triggers the optional draw for the spellbomb's controller")
    void destructionTriggersDraw() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MoriokReaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Nihil Spellbomb"));
        harness.assertInGraveyard(player1, "Nihil Spellbomb");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Moriok Reaver");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards put into the graveyard in response are also exiled")
    void exilesCardsAddedBeforeResolution() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver()));
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertInGraveyard(player2, "Galvanic Blast");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactlyInAnyOrder("Moriok Reaver", "Galvanic Blast");
        harness.assertInGraveyard(player1, "Nihil Spellbomb");
    }

    @Test
    @DisplayName("A tapped spellbomb cannot pay its activation cost")
    void tappedSpellbombCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new NihilSpellbomb()).setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nihil Spellbomb");
        harness.assertNotInGraveyard(player1, "Nihil Spellbomb");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both abilities work: graveyard exiled AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new MoriokReaver(), new GalvanicBlast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Accept death trigger - pay {B} to draw, inner DrawCardEffect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        harness.passBothPriorities(); // resolve ExileGraveyard

        // Graveyard exiled
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }
}
