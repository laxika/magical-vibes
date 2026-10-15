package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AutomatedArtificer;
import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.cards.f.FavorOfJukai;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrilliantRestoration.class, GhostlyPrison.class, GrizzlyBears.class, IchorWellspring.class, AutomatedArtificer.class, FangOfShigeki.class, FavorOfJukai.class})
class BrilliantRestorationTest extends BaseCardTest {

    @Test
    void returnsAllOwnArtifactsAndEnchantmentsFromGraveyard() {
        Card artifact = new IchorWellspring();
        Card enchantment = new GhostlyPrison();
        Card creature = new GrizzlyBears();
        Card opponentArtifact = new IchorWellspring();
        Card restoration = new BrilliantRestoration();
        harness.setGraveyard(player1, List.of(artifact, enchantment, creature));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(restoration));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artifact, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, restoration);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    void resolvesWithAnEmptyGraveyard() {
        Card restoration = new BrilliantRestoration();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(restoration));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(restoration);
    }

    @Test
    void returnsArtifactAndEnchantmentCreaturesUntapped() {
        Card artifactCreature = new AutomatedArtificer();
        Card enchantmentCreature = new FangOfShigeki();
        harness.setGraveyard(player1, List.of(artifactCreature, enchantmentCreature));
        harness.setHand(player1, List.of(new BrilliantRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artifactCreature, enchantmentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
    }

    @Test
    void returnsAuraAttachedToAnExistingLegalPermanent() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        Card aura = new FavorOfJukai();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new BrilliantRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(p -> {
            assertThat(p.getCard()).isSameAs(aura);
            assertThat(p.getAttachedTo()).isEqualTo(host.getId());
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void auraCannotEnchantAPermanentReturningAtTheSameTime() {
        Card artifactCreature = new AutomatedArtificer();
        Card aura = new FavorOfJukai();
        harness.setGraveyard(player1, List.of(artifactCreature, aura));
        harness.setHand(player1, List.of(new BrilliantRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(artifactCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
    }
}

