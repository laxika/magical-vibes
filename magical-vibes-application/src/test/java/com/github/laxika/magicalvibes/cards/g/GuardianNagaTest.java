package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BanishingCoils;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianNaga.class, BanishingCoils.class, FountainOfYouth.class, GloriousAnthem.class,
        GrizzlyBears.class, Shock.class})
class GuardianNagaTest extends BaseCardTest {

    @Test
    void adventureExilesTargetArtifactAndAllowsTheCreatureToBeCastLater() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        GuardianNaga card = new GuardianNaga();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureExilesTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        GuardianNaga card = new GuardianNaga();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianNaga()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    void preventsAllDamageToItDuringItsControllersTurn() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, naga.getId());

        assertThat(naga.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotPreventDamageToItDuringAnOpponentsTurn() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, naga.getId());

        assertThat(naga.getMarkedDamage()).isEqualTo(2);
    }
}
