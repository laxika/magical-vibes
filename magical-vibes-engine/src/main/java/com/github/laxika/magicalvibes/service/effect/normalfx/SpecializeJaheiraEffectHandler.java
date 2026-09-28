package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.j.JaheiraHarperEmissary;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeJaheiraEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Applies Jaheira, Harper Emissary's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeJaheiraEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeJaheiraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var specialize = (SpecializeJaheiraEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Jaheira, Harper Emissary".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        JaheiraHarperEmissary.setSpecializedBaseCharacteristics(specialized);
        JaheiraHarperEmissary.addHexproofFromArtifactsAndEnchantments(specialized);
        specialized.setPower(3);
        specialized.setToughness(4);

        List<CardEffect> specializedTrigger = JaheiraHarperEmissary.specializedTrigger(specialize.color());
        setFaceCharacteristics(specialized, specialize.color());
        JaheiraHarperEmissary.installSpecializedTargets(specialized, specializedTrigger, specialize.color());
        source.exchangeCard(specialized);

        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                specialized,
                entry.getControllerId(),
                specializedTrigger,
                entry.getSourcePermanentId(),
                List.of(),
                0,
                0,
                List.of(),
                0,
                List.of(),
                false,
                null,
                null));
    }

    private void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Jaheira, Heroic Harper");
                card.setManaCost("{1}{G}{W}");
                card.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "Put a +1/+1 counter on each of up to two other target creatures.");
            }
            case BLUE -> {
                card.setName("Jaheira, Insightful Harper");
                card.setManaCost("{1}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "Scry 2.");
            }
            case BLACK -> {
                card.setName("Jaheira, Ruthless Harper");
                card.setManaCost("{1}{G}{B}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColor(CardColor.BLACK);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "Each opponent loses 3 life.");
            }
            case RED -> {
                card.setName("Jaheira, Stirring Harper");
                card.setManaCost("{1}{G}{R}");
                card.setColors(List.of(CardColor.GREEN, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.RED));
                card.setColor(CardColor.RED);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "You get a one-time boon with \"When you cast a creature spell, it perpetually gets "
                        + "+1/+0 and gains haste.\"");
            }
            case GREEN -> {
                card.setName("Jaheira, Merciful Harper");
                card.setManaCost("{1}{G}{G}");
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "You gain 4 life.");
            }
            default -> throw new IllegalStateException("Unsupported Jaheira specialization color: " + color);
        }
    }

}
