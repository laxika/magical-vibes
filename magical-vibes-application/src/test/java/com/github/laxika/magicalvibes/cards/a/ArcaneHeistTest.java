package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ArcaneHeist.class, Shock.class, GrizzlyBears.class})
class ArcaneHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an opponent's instant for free and exiles it")
    void castsOpponentsInstantForFreeAndExilesIt() {
        Shock shock = new Shock();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ArcaneHeist heist = new ArcaneHeist();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(heist));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId).contains(shock.getId());
        harness.assertInGraveyard(player1, "Arcane Heist");
    }

    @Test
    @DisplayName("Cipher casts another Arcane Heist copy after combat damage")
    void cipherCastsCopyAfterCombatDamage() {
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        ArcaneHeist heist = new ArcaneHeist();
        harness.setGraveyard(player2, List.of(firstShock, secondShock));
        harness.setHand(player1, List.of(heist));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, firstShock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).contains(heist.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondShock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstShock.getId(), secondShock.getId());
    }

    @Test
    @DisplayName("Only an instant or sorcery in an opponent's graveyard can be targeted")
    void rejectsInvalidGraveyardTargets() {
        Card ownShock = new Shock();
        harness.setGraveyard(player1, List.of(ownShock));
        harness.setHand(player1, List.of(new ArcaneHeist()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownShock.getId()))
                .isInstanceOf(IllegalStateException.class);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ArcaneHeist()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
