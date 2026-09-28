package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "150")
@CardRegistration(set = "PIP", collectorNumber = "442")
@CardRegistration(set = "PIP", collectorNumber = "678")
@CardRegistration(set = "PIP", collectorNumber = "970")
public class Junktown extends Card {

    public Junktown() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}",
                List.of(new SacrificeSelfCost(), junkTokens()),
                "{4}{R}, {T}, Sacrifice this land: Create three Junk tokens. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    private static CreateTokenEffect junkTokens() {
        return CreateTokenEffect.ofArtifactToken(
                3,
                "Junk",
                List.of(CardSubtype.JUNK),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new ExileTopCardMayPlayThisTurnEffect(false)),
                        "{T}, Sacrifice this token: Exile the top card of your library. You may play that card this turn. "
                                + "Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
