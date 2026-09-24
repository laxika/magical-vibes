package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "50")
public class HinterlandChef extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Almighty Brushwagg",
            "Frilled Sandwalla",
            "Moss Viper",
            "Brushstrider",
            "Highland Game",
            "Ironshell Beetle",
            "Lotus Cobra",
            "Kazandu Nectarpot",
            "Gilded Goose",
            "Nessian Hornbeetle",
            "Scurrid Colony",
            "Territorial Boar",
            "Deathbonnet Sprout",
            "Spore Crawler",
            "Moldgraf Millipede");

    public HinterlandChef() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DraftCardFromSpellbookEffect(SPELLBOOK),
                new PerpetuallyGrantCardCharacteristicsEffect(
                        CardType.ARTIFACT,
                        CardSubtype.FOOD,
                        new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this creature: You gain 3 life."
                        ))));
    }
}
