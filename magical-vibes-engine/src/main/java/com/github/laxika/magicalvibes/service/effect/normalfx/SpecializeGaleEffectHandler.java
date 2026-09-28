package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.g.GaleConduitOfTheArcane;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeGaleEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Applies Gale's five digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeGaleEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeGaleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeGaleEffect specialize = (SpecializeGaleEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Gale, Conduit of the Arcane".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        GaleConduitOfTheArcane.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(4);
        specialized.setToughness(5);

        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Gale, Holy Conduit");
                specialized.setManaCost("{3}{W}{U}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColor(CardColor.WHITE);
                specialized.setCardText("Whenever you cast an instant or sorcery spell, create a 1/1 white Pegasus creature token with flying.");
            }
            case BLUE -> {
                specialized.setName("Gale, Temporal Conduit");
                specialized.setManaCost("{3}{U}{U}");
                specialized.setCardText("Whenever you cast an instant or sorcery spell, draw a card, then discard a card.");
            }
            case BLACK -> {
                specialized.setName("Gale, Abyssal Conduit");
                specialized.setManaCost("{3}{U}{B}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColor(CardColor.BLACK);
                specialized.setCardText("Whenever you cast an instant or sorcery spell, each opponent loses 2 life.");
            }
            case RED -> {
                specialized.setName("Gale, Storm Conduit");
                specialized.setManaCost("{3}{U}{R}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColor(CardColor.RED);
                specialized.setCardText("Whenever you cast an instant or sorcery spell, Gale, Storm Conduit perpetually gains \"Creatures you control get +1/+0.\"");
            }
            case GREEN -> {
                specialized.setName("Gale, Primeval Conduit");
                specialized.setManaCost("{3}{G}{U}");
                specialized.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                specialized.setColor(CardColor.GREEN);
                specialized.setCardText("Whenever you cast an instant or sorcery spell, put two +1/+1 counters on target creature.");
            }
            default -> throw new IllegalStateException("Unsupported Gale specialization color: "
                    + specialize.color());
        }

        specialized.addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                GaleConduitOfTheArcane.specializedSpellCastTrigger(specialize.color()));
        source.exchangeCard(specialized);
    }
}
