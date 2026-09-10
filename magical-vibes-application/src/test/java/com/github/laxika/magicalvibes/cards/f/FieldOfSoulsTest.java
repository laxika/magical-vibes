package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.g.GoblinBombardment;
import com.github.laxika.magicalvibes.cards.s.Sarcomancy;
import com.github.laxika.magicalvibes.cards.w.WindsOfRath;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieldOfSouls.class, CanopySpider.class, WindsOfRath.class, Sarcomancy.class,
        GoblinBombardment.class, Capsize.class})
class FieldOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Your nontoken creature dying creates a 1/1 white Spirit with flying")
    void allyCreatureDeathCreatesSpirit() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player1, new CanopySpider());

        destroyUnenchantedCreaturesFromOpponent();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Two of your creatures dying creates two Spirits")
    void twoDeathsCreateTwoSpirits() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player1, new CanopySpider());
        harness.addToBattlefield(player1, new CanopySpider());

        destroyUnenchantedCreaturesFromOpponent();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's creature dying does not create a Spirit")
    void opponentCreatureDeathCreatesNothing() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player2, new CanopySpider());

        destroyUnenchantedCreaturesFromOpponent();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A token creature dying does not create a Spirit")
    void tokenCreatureDeathCreatesNothing() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.setHand(player1, List.of(new Sarcomancy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        Permanent bombardment = findPermanent(player1, "Goblin Bombardment");
        assertThat(zombie.getCard().isToken()).isTrue();

        harness.forceActivePlayer(player1);
        int bombardmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bombardment);
        harness.activateAbility(player1, bombardmentIndex, null, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A nontoken creature returned to hand does not create a Spirit")
    void creatureReturnedToHandCreatesNothing() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        harness.assertInHand(player1, "Canopy Spider");
    }

    /**
     * Has player2 cast Winds of Rath and resolves it plus any resulting triggers. Field of Souls is
     * an enchantment, so it survives the board wipe.
     */
    private void destroyUnenchantedCreaturesFromOpponent() {
        harness.setHand(player2, List.of(new WindsOfRath()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);
        resolveAllTriggers();
    }
}
