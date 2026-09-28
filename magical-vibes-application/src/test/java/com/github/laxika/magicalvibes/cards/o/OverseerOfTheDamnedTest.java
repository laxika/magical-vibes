package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverseerOfTheDamned.class, DoomBlade.class, GrizzlyBears.class})
class OverseerOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may destroy a target creature")
    void etbMayDestroyTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverseerOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB may be declined")
    void etbMayBeDeclined() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverseerOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's nontoken creature dying creates a tapped Zombie")
    void opponentNontokenCreatureDeathCreatesTappedZombie() {
        harness.addToBattlefield(player1, new OverseerOfTheDamned());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);
        harness.passBothPriorities();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Own and token creature deaths do not create Zombies")
    void ownAndTokenDeathsDoNotCreateZombies() {
        harness.addToBattlefield(player1, new OverseerOfTheDamned());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithDoomBlade(ownBears);
        assertThat(zombieTokens()).isEmpty();

        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());
        killWithDoomBlade(token);

        assertThat(zombieTokens()).isEmpty();
    }

    private void killWithDoomBlade(Permanent target) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private List<Permanent> zombieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie"))
                .toList();
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Saproling Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        card.setKeywords(Set.<Keyword>of());
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
