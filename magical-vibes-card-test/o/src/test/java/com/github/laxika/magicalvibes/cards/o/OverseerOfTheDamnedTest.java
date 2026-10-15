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

    @Test
    @DisplayName("The ETB destruction also triggers creation of a tapped Zombie")
    void etbDestructionCreatesZombie() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverseerOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(zombieTokens()).hasSize(1);
        Permanent zombie = zombieTokens().getFirst();
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("The ETB can destroy your own creature without creating a Zombie")
    void etbCanDestroyOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverseerOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(zombieTokens()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB can target Overseer itself even on an otherwise empty battlefield")
    void etbCanDestroyItself() {
        harness.setHand(player1, List.of(new OverseerOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent overseer = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, overseer.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Overseer of the Damned");
        assertThat(zombieTokens()).isEmpty();
    }

    @Test
    @DisplayName("Each opponent creature death creates its own Zombie")
    void multipleDeathsCreateMultipleZombies() {
        harness.addToBattlefield(player1, new OverseerOfTheDamned());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(first);
        harness.passBothPriorities();
        killWithDoomBlade(second);
        harness.passBothPriorities();

        assertThat(zombieTokens()).hasSize(2).allSatisfy(zombie -> assertThat(zombie.isTapped()).isTrue());
    }

    private void killWithDoomBlade(Permanent target) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
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
