package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderSlayerHatredHoned.class, AirElemental.class, GiantSpider.class, RabidBite.class})
class SpiderSlayerHatredHonedTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Spider it deals damage to")
    void destroysDamagedSpider() {
        harness.addToBattlefield(player1, new SpiderSlayerHatredHoned());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Spider-Slayer, Hatred Honed"),
                harness.getPermanentId(player2, "Giant Spider")));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Does not destroy a non-Spider it deals damage to")
    void doesNotDestroyNonSpider() {
        harness.addToBattlefield(player1, new SpiderSlayerHatredHoned());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Spider-Slayer, Hatred Honed"),
                harness.getPermanentId(player2, "Air Elemental")));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Graveyard ability exiles the source and creates two tapped Robot tokens")
    void graveyardAbilityCreatesRobots() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SpiderSlayerHatredHoned()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Spider-Slayer, Hatred Honed");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Spider-Slayer, Hatred Honed"));

        harness.passBothPriorities();

        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(2);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.getEffectivePower()).isEqualTo(1);
            assertThat(robot.getEffectiveToughness()).isEqualTo(1);
            assertThat(robot.getCard().getColor()).isNull();
            assertThat(gqs.isArtifact(robot)).isTrue();
            assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
            assertThat(robot.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(robot.isTapped()).isTrue();
        });
    }
}
