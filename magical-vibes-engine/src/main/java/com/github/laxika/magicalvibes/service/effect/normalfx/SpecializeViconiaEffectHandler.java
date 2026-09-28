package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.v.ViconiaNightsingersDisciple;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetExiledCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeViconiaEffect;
import com.github.laxika.magicalvibes.model.filter.ExiledCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Applies Viconia, Nightsinger's Disciple's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeViconiaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeViconiaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var specialize = (SpecializeViconiaEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Viconia, Nightsinger's Disciple".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        ViconiaNightsingersDisciple.setSpecializedBaseCharacteristics(specialized);
        ViconiaNightsingersDisciple.addExileAbility(specialized);
        specialized.setPower(3);
        specialized.setToughness(4);
        setFaceCharacteristics(specialized, specialize.color());

        List<ConjureDuplicateOfTargetExiledCardIntoHandEffect> triggers =
                ViconiaNightsingersDisciple.specializedTriggers(specialize.color());
        installSpecializedTargets(specialized, triggers);
        source.exchangeCard(specialized);

        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                specialized,
                entry.getControllerId(),
                new ArrayList<>(triggers),
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

    private void installSpecializedTargets(Card specialized,
                                           List<ConjureDuplicateOfTargetExiledCardIntoHandEffect> triggers) {
        int minimumTargets = triggers.size() == 2 ? 0 : 1;
        for (ConjureDuplicateOfTargetExiledCardIntoHandEffect trigger : triggers) {
            specialized.target(new ExiledCardPredicateTargetFilter(
                            trigger.filter(), "Target must be a matching card exiled with Viconia."),
                            minimumTargets, 1)
                    .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, trigger);
        }
    }

    private void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Viconia, Disciple of Rebirth");
                card.setManaCost("{1}{W}{B}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColor(CardColor.WHITE);
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gains \"You may "
                        + "spend mana as though it were mana of any color to cast this spell.\" If it has mana "
                        + "value 3 or less, you may put it onto the battlefield.\n"
                        + "{1}: Exile target card from a graveyard.");
            }
            case BLUE -> {
                card.setName("Viconia, Disciple of Arcana");
                card.setManaCost("{1}{U}{B}");
                card.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColor(CardColor.BLUE);
                card.setCardText("When this creature specializes, choose up to one target creature card and up "
                        + "to one target instant or sorcery card from among cards exiled with this creature. "
                        + "Conjure a duplicate of each of those cards into your hand. The duplicates perpetually "
                        + "gain \"You may spend mana as though it were mana of any color to cast this spell.\"\n"
                        + "{1}: Exile target card from a graveyard.");
            }
            case BLACK -> {
                card.setName("Viconia, Disciple of Blood");
                card.setManaCost("{1}{B}{B}");
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gains \"You may "
                        + "spend mana as though it were mana of any color to cast this spell\" and \"When this "
                        + "creature enters, each opponent loses 2 life and you gain 2 life.\"\n"
                        + "{1}: Exile target card from a graveyard.");
            }
            case RED -> {
                card.setName("Viconia, Disciple of Violence");
                card.setManaCost("{1}{B}{R}");
                card.setColors(List.of(CardColor.BLACK, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                card.setColor(CardColor.RED);
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gets +1/+0 and "
                        + "gains haste and \"You may spend mana as though it were mana of any color to cast this "
                        + "spell.\"\n{1}: Exile target card from a graveyard.");
            }
            case GREEN -> {
                card.setName("Viconia, Disciple of Strength");
                card.setManaCost("{1}{B}{G}");
                card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gets +2/+2 and "
                        + "gains \"You may spend mana as though it were mana of any color to cast this spell.\"\n"
                        + "{1}: Exile target card from a graveyard.");
            }
        }
    }
}
