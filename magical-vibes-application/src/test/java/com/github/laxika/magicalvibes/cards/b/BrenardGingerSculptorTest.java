package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrenardGingerSculptor.class, GrizzlyBears.class, Shock.class})
class BrenardGingerSculptorTest extends BaseCardTest {

    @Test
    void createsFoodGolemCopyWithAnthemAndFoodAbility() {
        harness.addToBattlefield(player1, new BrenardGingerSculptor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsCardId = bears.getCard().getId();

        killCreatureWithShock(player2, bears.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bearsCardId));
        Permanent copy = findPermanent(player1, "Grizzly Bears");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, copy)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, copy, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, copy, CardSubtype.GOLEM)).isTrue();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.TRAMPLE)).isTrue();

        copy.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy), 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void decliningLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new BrenardGingerSculptor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killCreatureWithShock(player2, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Grizzly Bears".equals(card.getName()));
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void triggersOnlyForAnotherControlledNontokenCreature() {
        harness.addToBattlefield(player1, new BrenardGingerSculptor());

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        killCreatureWithShock(player2, token.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        killCreatureWithShock(player1, opposingCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killCreatureWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
    }
}
