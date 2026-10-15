package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.r.RepositorySkaab;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeralfVisionaryStitcher.class, AzureDrake.class, RepositorySkaab.class})
class GeralfVisionaryStitcherTest extends BaseCardTest {

    @Test
    void sacrificesAnotherNontokenCreatureAndCreatesFlyingZombie() {
        Permanent geralf = addCreatureReady(player1, new GeralfVisionaryStitcher());
        Permanent drake = addCreatureReady(player1, new AzureDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Azure Drake");
        assertThat(geralf.isTapped()).isTrue();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, geralf, Keyword.FLYING)).isFalse();
        assertThat(drake).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void cannotSacrificeSourceOrToken() {
        addCreatureReady(player1, new GeralfVisionaryStitcher());
        harness.addToBattlefield(player1, createTokenCreature());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void geralfHasFlyingWhenHeIsAlsoAZombie() {
        Permanent geralf = addCreatureReady(player1, new GeralfVisionaryStitcher());
        geralf.getGrantedSubtypes().add(CardSubtype.ZOMBIE);

        assertThat(gqs.hasKeyword(gd, geralf, Keyword.FLYING)).isTrue();
    }

    @Test
    void grantsFlyingOnlyToControlledZombiesAndOnlyWhileOnBattlefield() {
        Permanent geralf = addCreatureReady(player1, new GeralfVisionaryStitcher());
        Permanent ownZombie = addCreatureReady(player1, new RepositorySkaab());
        Permanent opposingZombie = addCreatureReady(player2, new RepositorySkaab());

        assertThat(gqs.hasKeyword(gd, ownZombie, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingZombie, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, geralf, Keyword.FLYING)).isFalse();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geralf);

        assertThat(gqs.hasKeyword(gd, ownZombie, Keyword.FLYING)).isFalse();
    }

    @Test
    void tokenUsesModifiedToughnessAndAbilityResolvesWithoutGeralf() {
        Permanent geralf = addCreatureReady(player1, new GeralfVisionaryStitcher());
        Permanent skaab = addCreatureReady(player1, new RepositorySkaab());
        skaab.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        skaab.setToughnessModifier(1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Repository Skaab");
        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geralf);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isFalse();
    }

    @Test
    void chosenCreatureDeterminesTokenSizeWhenMultipleSacrificesAreAvailable() {
        addCreatureReady(player1, new GeralfVisionaryStitcher());
        Permanent unchosen = addCreatureReady(player1, new RepositorySkaab());
        Permanent chosen = addCreatureReady(player1, new RepositorySkaab());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(unchosen).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(chosen).isNotIn(gd.playerBattlefields.get(player1.getId()));
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(5);
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        addCreatureReady(player1, new GeralfVisionaryStitcher());
        addCreatureReady(player2, new RepositorySkaab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Repository Skaab");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new GeralfVisionaryStitcher());
        addCreatureReady(player1, new RepositorySkaab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Repository Skaab");
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Token Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }
}
