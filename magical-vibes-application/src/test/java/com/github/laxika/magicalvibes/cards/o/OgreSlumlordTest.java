package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RatColony;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OgreSlumlordTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature dying offers a may-token that creates a Rat")
    void createsRatTokenOnNontokenDeath() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat).isNotNull();
        assertThat(rat.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Declining the may-trigger creates no token")
    void decliningCreatesNothing() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rat"));
    }

    @Test
    @DisplayName("A dying token creature does not trigger")
    void tokenDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());

        killWithDoomBlade(token);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The created Rat token has deathtouch")
    void createdRatHasDeathtouch() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);
        harness.handleMayAbilityChosen(player1, true);

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Your other Rats gain deathtouch, opponent's Rats do not")
    void grantsDeathtouchOnlyToOwnRats() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        harness.addToBattlefield(player1, new RatColony());
        harness.addToBattlefield(player2, new RatColony());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Rat Colony"), Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Rat Colony"), Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Non-Rat creatures you control do not gain deathtouch")
    void doesNotGrantToNonRats() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch is lost when Ogre Slumlord leaves the battlefield")
    void deathtouchLostWhenSlumlordLeaves() {
        harness.addToBattlefield(player1, new OgreSlumlord());
        harness.addToBattlefield(player1, new RatColony());

        Permanent colony = findPermanent(player1, "Rat Colony");
        assertThat(gqs.hasKeyword(gd, colony, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Ogre Slumlord"));

        assertThat(gqs.hasKeyword(gd, colony, Keyword.DEATHTOUCH)).isFalse();
    }

    private void killWithDoomBlade(Permanent target) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities(); // resolve Doom Blade -> creature dies, trigger goes on the stack
        harness.passBothPriorities(); // resolve the triggered ability -> may prompt
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Saproling Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
