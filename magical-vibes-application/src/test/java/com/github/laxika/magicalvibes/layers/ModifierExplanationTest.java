package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.cards.b.BlackWard;
import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.t.TwistedImage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.layer.ModifierLine;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavior of {@code GameQueryService.explainStaticBonus} — the per-source attribution lines
 * behind the client's hover breakdown. The lines are display-only: these tests pin that each
 * kind of continuous effect is attributed to its source by name, that the returned bonus
 * matches {@code computeStaticBonus}, and that one-shot pumps stored on the permanent are
 * deliberately NOT attributed (the client reconciles them as an "Other effects" remainder).
 */
class ModifierExplanationTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        card.setOwnerId(player.getId());
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attach(Player controller, Card attachment, Permanent target) {
        attachment.setOwnerId(controller.getId());
        Permanent perm = new Permanent(attachment);
        perm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    private void castAndResolveInstantOn(Player player, Card card, Permanent target,
                                         ManaColor color, int colored) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, color, colored);
        harness.castAndResolveInstant(player, 0, target.getId());
    }

    private List<ModifierLine> lines(Permanent perm) {
        return gqs.explainStaticBonus(gd, perm).lines();
    }

    private ModifierLine lineFrom(List<ModifierLine> lines, String source) {
        return lines.stream().filter(l -> l.source().equals(source)).findFirst().orElse(null);
    }

    @Test
    @DisplayName("A static boost is attributed to its source by name")
    void staticBoostAttributedToSource() {
        addPermanent(player1, new GloriousAnthem());
        Permanent bears = addPermanent(player1, new GrizzlyBears());

        ModifierLine line = lineFrom(lines(bears), "Glorious Anthem");

        assertThat(line).isNotNull();
        assertThat(line.power()).isEqualTo(1);
        assertThat(line.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A lord's boost and keyword grant merge into one line per source")
    void lordBoostAndKeywordMergeIntoOneLine() {
        addPermanent(player1, new GoblinKing());
        Permanent goblin = addPermanent(player1, new RagingGoblin());

        List<ModifierLine> lines = lines(goblin);

        assertThat(lines.stream().filter(l -> l.source().equals("Goblin King"))).hasSize(1);
        ModifierLine line = lineFrom(lines, "Goblin King");
        assertThat(line.power()).isEqualTo(1);
        assertThat(line.toughness()).isEqualTo(1);
        assertThat(line.gainedKeywords()).contains(Keyword.MOUNTAINWALK);
    }

    @Test
    @DisplayName("An aura's boost and keyword grant are attributed to the aura")
    void auraAttributedByName() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        attach(player1, new Dub(), bears);

        ModifierLine line = lineFrom(lines(bears), "Dub");

        assertThat(line).isNotNull();
        assertThat(line.power()).isEqualTo(2);
        assertThat(line.toughness()).isEqualTo(2);
        assertThat(line.gainedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("An Aura's protection grant retains the Aura as its source")
    void auraProtectionAttributedByName() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        attach(player1, new BlackWard(), bears);

        GameQueryService.ExplainedBonus explained = gqs.explainStaticBonus(gd, bears);

        assertThat(explained.grantedEffectAttributions())
                .singleElement()
                .satisfies(attribution -> {
                    assertThat(attribution.sourceName()).isEqualTo("Black Ward");
                    assertThat(attribution.effect())
                            .isInstanceOfSatisfying(ProtectionFromColorsEffect.class,
                                    protection -> assertThat(protection.colors())
                                            .containsExactly(CardColor.BLACK));
                });
    }

    @Test
    @DisplayName("A base-P/T setter produces a base line plus a loses-all-abilities line")
    void baseSetterProducesBaseLine() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        attach(player1, new Lignify(), bears);

        List<ModifierLine> lines = lines(bears);

        // Lignify contributes two lines: the layer-6 ability removal and the 7b base set.
        ModifierLine baseLine = lines.stream()
                .filter(l -> l.source().equals("Lignify") && l.basePower() != null)
                .findFirst().orElse(null);
        assertThat(baseLine).isNotNull();
        assertThat(baseLine.basePower()).isEqualTo(0);
        assertThat(baseLine.baseToughness()).isEqualTo(4);
        assertThat(baseLine.power()).isZero();
        assertThat(baseLine.toughness()).isZero();
        assertThat(lines.stream().filter(l -> l.source().equals("Lignify") && l.losesAllAbilities()))
                .hasSize(1);
    }

    @Test
    @DisplayName("A P/T switch produces a switch line attributed to the resolved spell")
    void switchProducesSwitchLine() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        castAndResolveInstantOn(player1, new TwistedImage(), bears, ManaColor.BLUE, 1);

        ModifierLine line = lineFrom(lines(bears), "Twisted Image");

        assertThat(line).isNotNull();
        assertThat(line.switchesPt()).isTrue();
    }

    @Test
    @DisplayName("One-shot pumps stored on the permanent are not attributed")
    void oneShotPumpNotAttributed() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        castAndResolveInstantOn(player1, new GiantGrowth(), bears, ManaColor.GREEN, 1);

        // The +3/+3 lives in the permanent's own modifier, visible in the aggregates; the
        // attribution deliberately carries no line for it (client shows it as "Other effects").
        assertThat(lines(bears)).allSatisfy(line -> {
            assertThat(line.power()).isNotEqualTo(3);
        });
    }

    @Test
    @DisplayName("An unmodified permanent has no attribution lines")
    void unmodifiedPermanentHasNoLines() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());

        assertThat(lines(bears)).isEmpty();
    }

    @Test
    @DisplayName("The explained bonus matches computeStaticBonus")
    void explainedBonusMatchesComputeStaticBonus() {
        addPermanent(player1, new GloriousAnthem());
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        attach(player1, new Dub(), bears);

        GameQueryService.ExplainedBonus explained = gqs.explainStaticBonus(gd, bears);
        GameQueryService.StaticBonus direct = gqs.computeStaticBonus(gd, bears);

        assertThat(explained.bonus().power()).isEqualTo(direct.power());
        assertThat(explained.bonus().toughness()).isEqualTo(direct.toughness());
        assertThat(explained.bonus().keywords()).isEqualTo(direct.keywords());
    }
}
