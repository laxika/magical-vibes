package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GarrukUnleashed;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicAscension.class, AlpineWatchdog.class, GarrukUnleashed.class,
        Plains.class, Unsubstantiate.class})
class AngelicAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and gives its controller a 4/4 Angel token")
    void exilesCreatureAndCreatesAngelForItsController() {
        harness.addToBattlefield(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new AngelicAscension()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Alpine Watchdog"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Alpine Watchdog"));
        assertAngelToken(player2);
    }

    @Test
    @DisplayName("Exiles a planeswalker and gives its controller a 4/4 Angel token")
    void exilesPlaneswalkerAndCreatesAngelForItsController() {
        Permanent planeswalker = addReadyPlaneswalker(player2);
        harness.setHand(player1, List.of(new AngelicAscension()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Garruk, Unleashed");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Garruk, Unleashed"));
        assertAngelToken(player2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new AngelicAscension()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Can exile your own creature and create an Angel for you")
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new AngelicAscension()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertAngelToken(player1);
        assertThat(countPermanents(player2, "Angel")).isZero();
    }

    @Test
    @DisplayName("The controller gets the Angel while a stolen creature is exiled for its owner")
    void stolenCreatureGivesTokenToController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.setHand(player1, List.of(new AngelicAscension()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertAngelToken(player2);
        assertThat(countPermanents(player1, "Angel")).isZero();
    }

    @Test
    @DisplayName("Creates no Angel when the target leaves the battlefield before resolution")
    void noTokenWhenTargetIsBouncedInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new AngelicAscension(), new Unsubstantiate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(countPermanents(player2, "Angel")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling a creature token still gives its controller a new Angel")
    void exilingTokenCreatesReplacementAngel() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new AngelicAscension(), new AngelicAscension()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent originalAngel = findPermanent(player1, "Angel");

        harness.castInstant(player1, 0, originalAngel.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
        assertThat(findPermanent(player1, "Angel").getId()).isNotEqualTo(originalAngel.getId());
        assertAngelToken(player1);
        assertThat(countPermanents(player2, "Angel")).isZero();
    }

    private void assertAngelToken(Player player) {
        assertThat(gd.playerBattlefields.get(player.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Angel")
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getPower() == 4
                        && permanent.getCard().getToughness() == 4
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ANGEL)
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }

    private Permanent addReadyPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukUnleashed());
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
