package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReliveThePast.class, TormodsCrypt.class, Forest.class, PhyrexianArena.class, Pacifism.class})
class ReliveThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one artifact, land, and non-Aura enchantment as 5/5 Elementals")
    void returnsEachTargetTypeAndAnimatesThem() {
        TormodsCrypt artifact = new TormodsCrypt();
        Forest land = new Forest();
        PhyrexianArena enchantment = new PhyrexianArena();
        Pacifism aura = new Pacifism();
        harness.setGraveyard(player1, List.of(artifact, land, enchantment, aura));

        castReliveThePast();
        choose(artifact);
        choose(land);

        PendingInteraction.MultiGraveyardChoice finalChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(finalChoice.validCardIds()).containsExactly(enchantment.getId());
        assertThat(finalChoice.validCardIds()).doesNotContain(aura.getId());
        choose(enchantment);
        harness.passBothPriorities();

        Permanent returnedArtifact = returnedPermanent(artifact);
        Permanent returnedLand = returnedPermanent(land);
        Permanent returnedEnchantment = returnedPermanent(enchantment);
        for (Permanent returned : List.of(returnedArtifact, returnedLand, returnedEnchantment)) {
            assertThat(gqs.isCreature(gd, returned)).isTrue();
            assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.ELEMENTAL);
            assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
        }
        assertThat(gqs.isArtifact(returnedArtifact)).isTrue();
        assertThat(gqs.isLand(gd, returnedLand)).isTrue();
        assertThat(gqs.isEnchantment(gd, returnedEnchantment)).isTrue();
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Each target group is optional")
    void canDeclineEveryTargetGroup() {
        TormodsCrypt artifact = new TormodsCrypt();
        Forest land = new Forest();
        PhyrexianArena enchantment = new PhyrexianArena();
        harness.setGraveyard(player1, List.of(artifact, land, enchantment));

        castReliveThePast();
        chooseNothing();
        chooseNothing();
        chooseNothing();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Tormod's Crypt", "Forest", "Phyrexian Arena");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> List.of(artifact, land, enchantment).stream()
                        .anyMatch(card -> card.getId().equals(permanent.getCard().getId())));
    }

    private void castReliveThePast() {
        harness.setHand(player1, List.of(new ReliveThePast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
    }

    private void choose(Card card) {
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }

    private void chooseNothing() {
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    private Permanent returnedPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
