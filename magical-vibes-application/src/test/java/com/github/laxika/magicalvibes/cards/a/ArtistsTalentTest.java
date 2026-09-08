package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtistsTalent.class, Forest.class, GrizzlyBears.class, LightningStrike.class})
class ArtistsTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you cast a noncreature spell, you may discard a card to draw a card")
    void rummagesOnNoncreatureSpellCast() {
        harness.addToBattlefield(player1, new ArtistsTalent());
        Card discarded = new Forest();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new LightningStrike(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("At level 2, noncreature spells cost {1} less")
    void reducesNoncreatureSpellCost() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ArtistsTalent());
        levelUp(talent, 0);

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Lightning Strike"));
    }

    @Test
    @DisplayName("At level 3, noncombat damage to an opponent is increased by 2")
    void increasesNoncombatDamageToOpponent() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ArtistsTalent());
        levelUp(talent, 0);
        levelUp(talent, 1);

        harness.setHand(player1, List.of(new LightningStrike(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("At level 3, combat damage is not increased")
    void doesNotIncreaseCombatDamage() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ArtistsTalent());
        levelUp(talent, 0);
        levelUp(talent, 1);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void levelUp(Permanent talent, int abilityIndex) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }
}
